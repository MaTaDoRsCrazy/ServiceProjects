package ru.practice.servicedesk.config;

import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.practice.servicedesk.domain.EquipmentStatus;
import ru.practice.servicedesk.domain.EquipmentType;
import ru.practice.servicedesk.domain.RoleName;
import ru.practice.servicedesk.domain.TicketPriority;
import ru.practice.servicedesk.domain.TicketStatus;
import ru.practice.servicedesk.entity.Department;
import ru.practice.servicedesk.entity.Equipment;
import ru.practice.servicedesk.entity.Ticket;
import ru.practice.servicedesk.entity.TicketComment;
import ru.practice.servicedesk.entity.UserAccount;
import ru.practice.servicedesk.repository.DepartmentRepository;
import ru.practice.servicedesk.repository.EquipmentRepository;
import ru.practice.servicedesk.repository.TicketCommentRepository;
import ru.practice.servicedesk.repository.TicketRepository;
import ru.practice.servicedesk.repository.UserAccountRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(
            DepartmentRepository departmentRepository,
            UserAccountRepository userRepository,
            EquipmentRepository equipmentRepository,
            TicketRepository ticketRepository,
            TicketCommentRepository commentRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (departmentRepository.count() == 0) {
                departmentRepository.save(new Department("ИТ-отдел", "Кабинет 305"));
                departmentRepository.save(new Department("Бухгалтерия", "Кабинет 210"));
                departmentRepository.save(new Department("Отдел продаж", "Кабинет 115"));
            }

            if (userRepository.count() == 0) {
                userRepository.save(new UserAccount(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "Администратор системы",
                        "admin@example.com",
                        RoleName.ADMIN
                ));
                userRepository.save(new UserAccount(
                        "tech",
                        passwordEncoder.encode("tech123"),
                        "Иван Петров",
                        "tech@example.com",
                        RoleName.TECHNICIAN
                ));
                userRepository.save(new UserAccount(
                        "user",
                        passwordEncoder.encode("user123"),
                        "Мария Смирнова",
                        "user@example.com",
                        RoleName.EMPLOYEE
                ));
            }

            Department accounting = departmentRepository.findAll().stream()
                    .filter(department -> department.getName().equals("Бухгалтерия"))
                    .findFirst()
                    .orElseThrow();
            Department sales = departmentRepository.findAll().stream()
                    .filter(department -> department.getName().equals("Отдел продаж"))
                    .findFirst()
                    .orElseThrow();
            UserAccount employee = userRepository.findByUsername("user").orElseThrow();

            if (equipmentRepository.count() == 0) {
                Equipment laptop = new Equipment();
                laptop.setInventoryNumber("NB-2026-001");
                laptop.setTitle("Ноутбук Lenovo ThinkPad E14");
                laptop.setType(EquipmentType.LAPTOP);
                laptop.setStatus(EquipmentStatus.IN_SERVICE);
                laptop.setLocation("Кабинет 210");
                laptop.setSerialNumber("LEN-14-001");
                laptop.setAssignedTo(employee);
                laptop.setDepartment(accounting);
                laptop.setPurchaseDate(LocalDate.of(2025, 2, 15));
                equipmentRepository.save(laptop);

                Equipment printer = new Equipment();
                printer.setInventoryNumber("PR-2026-014");
                printer.setTitle("МФУ HP LaserJet Pro");
                printer.setType(EquipmentType.PRINTER);
                printer.setStatus(EquipmentStatus.REPAIR);
                printer.setLocation("Кабинет 115");
                printer.setSerialNumber("HP-LJ-014");
                printer.setDepartment(sales);
                printer.setPurchaseDate(LocalDate.of(2024, 11, 20));
                printer.setComment("Периодически застревает бумага");
                equipmentRepository.save(printer);
            }

            if (ticketRepository.count() == 0) {
                UserAccount admin = userRepository.findByUsername("admin").orElseThrow();
                UserAccount technician = userRepository.findByUsername("tech").orElseThrow();
                Equipment printer = equipmentRepository.findAll().stream()
                        .filter(equipment -> equipment.getInventoryNumber().equals("PR-2026-014"))
                        .findFirst()
                        .orElseThrow();

                Ticket ticket = new Ticket();
                ticket.setTitle("Не печатает МФУ в отделе продаж");
                ticket.setDescription("При отправке документа появляется ошибка замятия бумаги.");
                ticket.setPriority(TicketPriority.HIGH);
                ticket.setStatus(TicketStatus.IN_PROGRESS);
                ticket.setEquipment(printer);
                ticket.setCreatedBy(employee);
                ticket.setAssignedTo(technician);
                Ticket savedTicket = ticketRepository.save(ticket);

                commentRepository.save(new TicketComment(
                        savedTicket,
                        admin,
                        "Заявка принята, назначен ответственный техник."
                ));
            }
        };
    }
}

