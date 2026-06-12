package ru.practice.servicedesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practice.servicedesk.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}

