package ru.practice.servicedesk.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practice.servicedesk.dto.auth.AuthResponse;
import ru.practice.servicedesk.dto.auth.LoginRequest;
import ru.practice.servicedesk.dto.user.UserDto;
import ru.practice.servicedesk.entity.UserAccount;
import ru.practice.servicedesk.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal UserAccount user) {
        return UserDto.from(user);
    }
}

