package ru.haritonenko.eventmanager.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.eventmanager.user.api.converter.UserEntityConverter;
import ru.haritonenko.eventmanager.user.service.domain.User;
import ru.haritonenko.eventmanager.user.api.dto.registration.UserRegistration;
import ru.haritonenko.eventmanager.user.db.entity.UserEntity;
import ru.haritonenko.eventmanager.user.api.exception.UserAlreadyRegisteredException;
import ru.haritonenko.eventmanager.user.api.exception.UserNotFoundException;
import ru.haritonenko.eventmanager.user.db.repository.UserRepository;
import ru.haritonenko.eventmanager.user.api.role.UserRole;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserEntityConverter converter;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getUserById(Integer id) {
        log.info("Getting user by id: {}", id);
        var foundUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Error while getting user by id");
                    return new UserNotFoundException(
                            "No found user by id = %s".formatted(id));
                });
        log.info("User was successfully found by id: {}", id);
        return converter.toDomain(foundUser);
    }

    @Transactional
    public User register(UserRegistration userFromRegistration) {
        log.info("User registration started");
        if (userRepository.existsByLogin(userFromRegistration.login())) {
            log.error("Error while register user");
            throw new UserAlreadyRegisteredException("This user has already registered");
        }
        var hashedPass = passwordEncoder.encode(userFromRegistration.password());
        var userToSave = new UserEntity(
                null,
                userFromRegistration.login(),
                hashedPass,
                userFromRegistration.age(),
                UserRole.USER
        );
        var savedUserEntity = userRepository.save(userToSave);
        log.info("User has successfully registered");
        return converter.toDomain(savedUserEntity);
    }

    public User findByLogin(String login) {
        log.info("Searching for user by login: {}", login);
        var foundUser = userRepository.findByLogin(login)
                .orElseThrow(() -> {
                    log.error("Error while finding user by login");
                    return new UserNotFoundException("User not found");
                });
        log.info("User was successfully found by login: {}", login);
        return converter.toDomain(foundUser);
    }
}
