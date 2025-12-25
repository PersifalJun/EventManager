package ru.haritonenko.eventmanager.user.db.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.eventmanager.user.db.entity.UserEntity;
import ru.haritonenko.eventmanager.user.db.repository.UserRepository;
import ru.haritonenko.eventmanager.user.api.role.UserRole;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultUsersInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        createIfNotExists("admin", "admin", UserRole.ADMIN);
        createIfNotExists("user", "user", UserRole.USER);
    }

    private void createIfNotExists(String login, String rawPassword, UserRole role) {
        if (userRepository.existsByLogin(login)) {
            log.warn("Default user '{}' already exists, skipping", login);
            return;
        }
        UserEntity entity = new UserEntity(
                null,
                login,
                passwordEncoder.encode(rawPassword),
                21,
                role
        );
        userRepository.save(entity);
        log.info("Default user '{}' created with role {}", login, role);
    }
}