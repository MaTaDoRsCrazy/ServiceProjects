package ru.practice.servicedesk.dto.comment;

import java.time.LocalDateTime;
import ru.practice.servicedesk.entity.TicketComment;

public record CommentResponse(
        Long id,
        Long ticketId,
        Long authorId,
        String authorName,
        String message,
        LocalDateTime createdAt
) {
    public static CommentResponse from(TicketComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getFullName(),
                comment.getMessage(),
                comment.getCreatedAt()
        );
    }
}

