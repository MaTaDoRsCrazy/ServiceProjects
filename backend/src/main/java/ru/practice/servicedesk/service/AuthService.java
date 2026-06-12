package ru.practice.servicedesk.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.practice.servicedesk.dto.auth.AuthResponse;
import ru.practice.servicedesk.dto.auth.LoginRequest;
import ru.practice.servicedesk.dto.user.UserDto;
import ru.practice.servicedesk.entity.UserAccount;
import ru.practice.servicedesk.security.JwtService;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        UserAccount user = (UserAccount) authentication.getPrincipal();
        return new AuthResponse(jwtService.generateToken(user), UserDto.from(user));
    }
}

