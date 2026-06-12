package ru.practice.servicedesk.dto.ticket;

import java.time.LocalDateTime;
import ru.practice.servicedesk.domain.TicketPriority;
import ru.practice.servicedesk.domain.TicketStatus;
import ru.practice.servicedesk.entity.Ticket;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        Long equipmentId,
        String equipmentTitle,
        Long createdById,
        String createdByName,
        Long assignedToId,
        String assignedToName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getEquipment() == null ? null : ticket.getEquipment().getId(),
                ticket.getEquipment() == null ? null : ticket.getEquipment().getTitle(),
                ticket.getCreatedBy().getId(),
                ticket.getCreatedBy().getFullName(),
                ticket.getAssignedTo() == null ? null : ticket.getAssignedTo().getId(),
                ticket.getAssignedTo() == null ? null : ticket.getAssignedTo().getFullName(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt()
        );
    }
}

