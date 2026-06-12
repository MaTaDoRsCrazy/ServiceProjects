package ru.practice.servicedesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practice.servicedesk.domain.EquipmentStatus;
import ru.practice.servicedesk.entity.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    long countByStatus(EquipmentStatus status);
}

