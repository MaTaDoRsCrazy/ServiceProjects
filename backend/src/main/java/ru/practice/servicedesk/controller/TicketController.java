package ru.practice.servicedesk.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practice.servicedesk.domain.TicketStatus;
import ru.practice.servicedesk.dto.comment.CommentRequest;
import ru.practice.servicedesk.dto.comment.CommentResponse;
import ru.practice.servicedesk.dto.ticket.TicketCreateRequest;
import ru.practice.servicedesk.dto.ticket.TicketResponse;
import ru.practice.servicedesk.dto.ticket.TicketStatusUpdateRequest;
import ru.practice.servicedesk.dto.ticket.TicketUpdateRequest;
import ru.practice.servicedesk.entity.UserAccount;
import ru.practice.servicedesk.service.TicketService;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketResponse> findAll(
            @RequestParam(required = false) TicketStatus status,
            @AuthenticationPrincipal UserAccount currentUser
    ) {
        return ticketService.findAll(status, currentUser);
    }

    @GetMapping("/{id}")
    public TicketResponse findById(@PathVariable Long id, @AuthenticationPrincipal UserAccount currentUser) {
        return ticketService.findById(id, currentUser);
    }

    @PostMapping
    public TicketResponse create(
            @Valid @RequestBody TicketCreateRequest request,
            @AuthenticationPrincipal UserAccount currentUser
    ) {
        return ticketService.create(request, currentUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
    public TicketResponse update(@PathVariable Long id, @Valid @RequestBody TicketUpdateRequest request) {
        return ticketService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
    public TicketResponse changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusUpdateRequest request,
            @AuthenticationPrincipal UserAccount currentUser
    ) {
        return ticketService.changeStatus(id, request, currentUser);
    }

    @GetMapping("/{id}/comments")
    public List<CommentResponse> comments(@PathVariable Long id, @AuthenticationPrincipal UserAccount currentUser) {
        return ticketService.findComments(id, currentUser);
    }

    @PostMapping("/{id}/comments")
    public CommentResponse addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserAccount currentUser
    ) {
        return ticketService.addComment(id, request, currentUser);
    }
}

