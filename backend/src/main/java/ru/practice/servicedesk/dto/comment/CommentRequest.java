package ru.practice.servicedesk.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(@NotBlank String message) {
}

