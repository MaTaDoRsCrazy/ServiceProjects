package ru.practice.servicedesk.dto.ticket;

import jakarta.validation.constraints.NotNull;
import ru.practice.servicedesk.domain.TicketStatus;

public record TicketStatusUpdateRequest(
        @NotNull TicketStatus status,
        String comment
) {
}

