package ru.haritonenko.eventmanager.event.api.filter;

import ru.haritonenko.eventmanager.event.api.status.EventStatus;

public record EventSearchRequestDto(
        String name,
        Integer placesMin,
        Integer placesMax,
        String dateStartAfter,
        String dateStartBefore,
        Number costMin,
        Number costMax,
        Integer durationMin,
        Integer durationMax,
        Integer locationId,
        EventStatus eventStatus
) {
}
