package ru.haritonenko.eventmanager.event.api.dto;

public record EventCreateRequestDto(
        String name,
        Integer maxPlaces,
        String date,
        Integer cost,
        Integer duration,
        Integer locationId
) {
}
