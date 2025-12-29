package ru.haritonenko.eventmanager.event.service.domain;

import ru.haritonenko.eventmanager.event.api.status.EventStatus;

import java.math.BigDecimal;

public record Event(
        Integer id,
        String name,
        String ownerId,
        Integer maxPlaces,
        Integer occupiedPlaces,
        String date,
        BigDecimal cost,
        Integer duration,
        Integer locationId,
        EventStatus status
) {
}
