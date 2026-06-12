package ru.practice.servicedesk.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import ru.practice.servicedesk.domain.TicketPriority;

public record TicketCreateRequest(
        @NotBlank String title,
        @NotBlank String description,
        TicketPriority priority,
        Long equipmentId
) {
}

