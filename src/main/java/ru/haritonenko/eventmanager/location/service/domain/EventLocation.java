package ru.haritonenko.eventmanager.location.service.domain;

public record EventLocation(
        Integer id,
        String name,
        String address,
        Integer capacity,
        String description
) {
}
