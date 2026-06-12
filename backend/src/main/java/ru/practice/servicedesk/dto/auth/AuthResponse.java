package ru.practice.servicedesk.dto.auth;

import ru.practice.servicedesk.dto.user.UserDto;

public record AuthResponse(String token, UserDto user) {
}

