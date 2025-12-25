package ru.haritonenko.eventmanager.location.api.dto.filter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EventLocationSearchFilter(
        @NotBlank(message = "Location name can not be blank")
        @Size(min = 1, max = 40, message = "Min name size is 1, max is 40")
        String name,
        @NotBlank(message = "Location address can not be blank")
        @Size(min = 5, max = 30, message = "Min address size is 5, max is 30")
        String address,
        @Min(0)
        Integer pageNumber,
        @Min(3)
        Integer pageSize
) {
}
