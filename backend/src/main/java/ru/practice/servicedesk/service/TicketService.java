package ru.practice.servicedesk.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practice.servicedesk.domain.RoleName;
import ru.practice.servicedesk.domain.TicketPriority;
import ru.practice.servicedesk.domain.TicketStatus;
import ru.practice.servicedesk.dto.comment.CommentRequest;
import ru.practice.servicedesk.dto.comment.CommentResponse;
import ru.practice.servicedesk.dto.ticket.TicketCreateRequest;
import ru.practice.servicedesk.dto.ticket.TicketResponse;
import ru.practice.servicedesk.dto.ticket.TicketStatusUpdateRequest;
import ru.practice.servicedesk.dto.ticket.TicketUpdateRequest;
import ru.practice.servicedesk.entity.Equipment;
import ru.practice.servicedesk.entity.Ticket;
import ru.practice.servicedesk.entity.TicketComment;
import ru.practice.servicedesk.entity.UserAccount;
import ru.practice.servicedesk.exception.ResourceNotFoundException;
import ru.practice.servicedesk.repository.EquipmentRepository;
import ru.practice.servicedesk.repository.TicketCommentRepository;
import ru.practice.servicedesk.repository.TicketRepository;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserService userService;

    public TicketService(
            TicketRepository ticketRepository,
            TicketCommentRepository commentRepository,
            EquipmentRepository equipmentRepository,
            UserService userService
    ) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.equipmentRepository = equipmentRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> findAll(TicketStatus status, UserAccount currentUser) {
        boolean onlyOwnTickets = currentUser.getRole() == RoleName.EMPLOYEE;
        List<Ticket> tickets;
        if (onlyOwnTickets && status != null) {
            tickets = ticketRepository.findByCreatedByIdAndStatusOrderByCreatedAtDesc(currentUser.getId(), status);
        } else if (onlyOwnTickets) {
            tickets = ticketRepository.findByCreatedByIdOrderByCreatedAtDesc(currentUser.getId());
        } else if (status != null) {
            tickets = ticketRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        }
        return tickets.stream().map(TicketResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse findById(Long id, UserAccount currentUser) {
        return TicketResponse.from(getTicketForUser(id, currentUser));
    }

    @Transactional
    public TicketResponse create(TicketCreateRequest request, UserAccount currentUser) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority() == null ? TicketPriority.MEDIUM : request.priority());
        ticket.setStatus(TicketStatus.NEW);
        ticket.setCreatedBy(currentUser);
        ticket.setEquipment(request.equipmentId() == null ? null : getEquipment(request.equipmentId()));
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse update(Long id, TicketUpdateRequest request) {
        Ticket ticket = getTicket(id);
        if (StringUtils.hasText(request.title())) {
            ticket.setTitle(request.title());
        }
        if (StringUtils.hasText(request.description())) {
            ticket.setDescription(request.description());
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        if (request.status() != null) {
            applyStatus(ticket, request.status());
        }
        if (request.equipmentId() != null) {
            ticket.setEquipment(getEquipment(request.equipmentId()));
        }
        if (request.assignedToId() != null) {
            ticket.setAssignedTo(userService.findById(request.assignedToId()));
        }
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse changeStatus(Long id, TicketStatusUpdateRequest request, UserAccount currentUser) {
        Ticket ticket = getTicket(id);
        applyStatus(ticket, request.status());
        Ticket savedTicket = ticketRepository.save(ticket);
        if (StringUtils.hasText(request.comment())) {
            commentRepository.save(new TicketComment(savedTicket, currentUser, request.comment()));
        }
        return TicketResponse.from(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findComments(Long ticketId, UserAccount currentUser) {
        getTicketForUser(ticketId, currentUser);
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(Long ticketId, CommentRequest request, UserAccount currentUser) {
        Ticket ticket = getTicketForUser(ticketId, currentUser);
        TicketComment comment = new TicketComment(ticket, currentUser, request.message());
        return CommentResponse.from(commentRepository.save(comment));
    }

    private void applyStatus(Ticket ticket, TicketStatus status) {
        ticket.setStatus(status);
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        } else {
            ticket.setClosedAt(null);
        }
    }

    private Ticket getTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена: " + id));
    }

    private Ticket getTicketForUser(Long id, UserAccount currentUser) {
        Ticket ticket = getTicket(id);
        if (currentUser.getRole() == RoleName.EMPLOYEE && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Пользователь может просматривать только свои заявки");
        }
        return ticket;
    }

    private Equipment getEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Оборудование не найдено: " + id));
    }
}

