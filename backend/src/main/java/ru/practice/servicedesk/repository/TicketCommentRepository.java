package ru.practice.servicedesk.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practice.servicedesk.entity.TicketComment;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}

