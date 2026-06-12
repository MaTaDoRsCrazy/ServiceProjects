package ru.practice.servicedesk.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practice.servicedesk.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
}

