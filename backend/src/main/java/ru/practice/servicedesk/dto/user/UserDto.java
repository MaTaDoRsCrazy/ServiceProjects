package ru.practice.servicedesk.dto.user;

import ru.practice.servicedesk.domain.RoleName;
import ru.practice.servicedesk.entity.UserAccount;

public record UserDto(
        Long id,
        String username,
        String fullName,
        String email,
        RoleName role
) {
    public static UserDto from(UserAccount user) {
        return new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole());
    }
}

