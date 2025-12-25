package ru.haritonenko.eventmanager.location.api.filter;

import jakarta.validation.constraints.Min;

public record EventLocationSearchFilter(
        String name,
        String address,
        @Min(0)
        Integer pageNumber,
        @Min(3)
        Integer pageSize
) {
}
