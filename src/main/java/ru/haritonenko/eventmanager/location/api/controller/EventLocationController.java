package ru.haritonenko.eventmanager.location.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.haritonenko.eventmanager.location.api.converter.EventLocationDtoConverter;
import ru.haritonenko.eventmanager.location.api.dto.EventLocationDto;
import ru.haritonenko.eventmanager.location.api.dto.filter.EventLocationSearchFilter;
import ru.haritonenko.eventmanager.location.service.EventLocationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class EventLocationController {

    private final EventLocationService locationService;
    private final EventLocationDtoConverter converter;

    @GetMapping
    public List<EventLocationDto> searchAllLocations(
            @Valid EventLocationSearchFilter locationFilter
    ) {
        log.info("Get request for getting all locations");
        return locationService.getAllLocations(locationFilter)
                .stream()
                .map(converter::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public EventLocationDto getById(
            @PathVariable Integer id
    ) {
        log.info("Get request for getting location by id: {}", id);
        var foundLocation = locationService.getLocationById(id);
        return converter.toDto(foundLocation);
    }

    @PostMapping
    public ResponseEntity<EventLocationDto> createLocation(
            @RequestBody @Valid EventLocationDto locationFromCreateRequest
    ) {
        log.info("Post request for creation a new location: {}", locationFromCreateRequest);
        var createdLocation = locationService
                .createLocation(converter.toDomain(locationFromCreateRequest));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(converter.toDto(createdLocation));
    }

    @PutMapping("/{id}")
    public EventLocationDto updateLocation(
            @PathVariable Integer id,
            @RequestBody @Valid EventLocationDto locationFromUpdateRequest
    ) {
        log.info("Put request for updating location: {}", locationFromUpdateRequest);
        var updatedLocation = locationService
                .updateLocation(id, converter.toDomain(locationFromUpdateRequest));
        return converter.toDto(updatedLocation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable Integer id
    ) {
        log.info("Delete request for deleting location by id: {}", id);
        locationService.deleteLocation(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
