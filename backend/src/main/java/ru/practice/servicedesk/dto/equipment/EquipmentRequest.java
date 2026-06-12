package ru.practice.servicedesk.dto.equipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import ru.practice.servicedesk.domain.EquipmentStatus;
import ru.practice.servicedesk.domain.EquipmentType;

public record EquipmentRequest(
        @NotBlank String inventoryNumber,
        @NotBlank String title,
        @NotNull EquipmentType type,
        @NotNull EquipmentStatus status,
        @NotBlank String location,
        String serialNumber,
        Long assignedToId,
        Long departmentId,
        LocalDate purchaseDate,
        String comment
) {
}

