package ru.haritonenko.eventmanager.event.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import ru.haritonenko.eventmanager.event.api.status.EventStatus;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventDto(
        @Null
        Integer id,
        @NotBlank(message = "Location name can not be blank")
        @Size(min = 1, max = 40,message = "Min name size is 1, max is 40")
        String name,
        String ownerId,
        Integer maxPlaces,
        Integer occupiedPlaces,
        String date,
        BigDecimal cost,
        Integer duration,
        Integer locationId,
        EventStatus status

) {}
