package ru.practice.servicedesk.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import ru.practice.servicedesk.domain.RoleName;
import ru.practice.servicedesk.entity.UserAccount;

class JwtServiceTest {

    @Test
    void generatedTokenContainsUsernameAndCanBeValidated() {
        JwtService jwtService = new JwtService("test-secret-key-that-is-long-enough-for-hmac-sha256", 3_600_000);
        UserAccount user = new UserAccount("admin", "password", "Admin", "admin@example.com", RoleName.ADMIN);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }
}

