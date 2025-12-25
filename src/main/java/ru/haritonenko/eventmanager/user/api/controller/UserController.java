package ru.haritonenko.eventmanager.user.api.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.haritonenko.eventmanager.security.jwt.JwtAuthenticationService;
import ru.haritonenko.eventmanager.security.jwt.JwtResponse;
import ru.haritonenko.eventmanager.user.api.converter.UserDtoConverter;
import ru.haritonenko.eventmanager.user.api.dto.UserDto;
import ru.haritonenko.eventmanager.user.api.dto.authorization.UserCredentials;
import ru.haritonenko.eventmanager.user.api.dto.registration.UserRegistration;
import ru.haritonenko.eventmanager.user.domain.service.UserService;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserDtoConverter converter;
    private final JwtAuthenticationService jwtAuthenticationService;

    public UserController(UserService userService, UserDtoConverter converter, JwtAuthenticationService jwtAuthenticationService) {
        this.userService = userService;
        this.converter = converter;
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserDto getUserById(
            @PathVariable Integer id
    ) {
        log.info("Get request for getting user by id: {}", id);
        var foundUser = userService.getUserById(id);
        return converter.toDto(foundUser);
    }

    @PostMapping
    public ResponseEntity<UserDto> registerUser(
            @Valid @RequestBody UserRegistration userFromSignUpRequest
    ) {
        log.info("Post request for sign-up login: {}", userFromSignUpRequest.login());
        var registeredUser = userService.register(userFromSignUpRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(converter.toDto(registeredUser));
    }

    @PostMapping("/auth")
    public ResponseEntity<JwtResponse> authenticateUser(
            @Valid @RequestBody UserCredentials userFromSignInRequest
    ) {
        log.info("Post request for authenticating login: {}", userFromSignInRequest.login());
        var token = jwtAuthenticationService.authenticate(userFromSignInRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new JwtResponse(token));
    }
}
