package ru.haritonenko.eventmanager.event.api.dto;

import java.math.BigDecimal;

public record EventUpdateRequestDto(
    String name,
    Integer maxPlaces,
    String date,
    BigDecimal cost,
    Integer duration,
    Integer locationId
) {
}
