package ru.practice.servicedesk.dto.equipment;

import java.time.LocalDate;
import ru.practice.servicedesk.domain.EquipmentStatus;
import ru.practice.servicedesk.domain.EquipmentType;
import ru.practice.servicedesk.entity.Equipment;

public record EquipmentResponse(
        Long id,
        String inventoryNumber,
        String title,
        EquipmentType type,
        EquipmentStatus status,
        String location,
        String serialNumber,
        Long assignedToId,
        String assignedToName,
        Long departmentId,
        String departmentName,
        LocalDate purchaseDate,
        String comment
) {
    public static EquipmentResponse from(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getId(),
                equipment.getInventoryNumber(),
                equipment.getTitle(),
                equipment.getType(),
                equipment.getStatus(),
                equipment.getLocation(),
                equipment.getSerialNumber(),
                equipment.getAssignedTo() == null ? null : equipment.getAssignedTo().getId(),
                equipment.getAssignedTo() == null ? null : equipment.getAssignedTo().getFullName(),
                equipment.getDepartment() == null ? null : equipment.getDepartment().getId(),
                equipment.getDepartment() == null ? null : equipment.getDepartment().getName(),
                equipment.getPurchaseDate(),
                equipment.getComment()
        );
    }
}

