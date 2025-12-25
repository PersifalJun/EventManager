package ru.haritonenko.eventmanager.location.api.converter;

import org.springframework.stereotype.Component;
import ru.haritonenko.eventmanager.location.domain.EventLocation;
import ru.haritonenko.eventmanager.location.api.dto.EventLocationDto;

@Component
public class EventLocationDtoConverter {

    public EventLocationDto toDto(EventLocation eventLocation) {
        return new EventLocationDto(
                eventLocation.id(),
                eventLocation.name(),
                eventLocation.address(),
                eventLocation.capacity(),
                eventLocation.description()
        );
    }

    public EventLocation toDomain(EventLocationDto eventLocationDto) {
        return new EventLocation(
                eventLocationDto.id(),
                eventLocationDto.name(),
                eventLocationDto.address(),
                eventLocationDto.capacity(),
                eventLocationDto.description()
        );
    }
}
