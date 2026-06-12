package ru.practice.servicedesk.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practice.servicedesk.domain.TicketStatus;
import ru.practice.servicedesk.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByStatus(TicketStatus status);

    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<Ticket> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    List<Ticket> findByCreatedByIdAndStatusOrderByCreatedAtDesc(Long userId, TicketStatus status);
}

