package ru.practice.servicedesk.dto.ticket;

import ru.practice.servicedesk.domain.TicketPriority;
import ru.practice.servicedesk.domain.TicketStatus;

public record TicketUpdateRequest(
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        Long equipmentId,
        Long assignedToId
) {
}

