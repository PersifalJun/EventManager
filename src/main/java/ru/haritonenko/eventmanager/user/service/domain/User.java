package ru.haritonenko.eventmanager.user.service.domain;

import ru.haritonenko.eventmanager.user.api.role.UserRole;

public record User(
        Integer id,
        String login,
        int age,
        UserRole role
) {
}
