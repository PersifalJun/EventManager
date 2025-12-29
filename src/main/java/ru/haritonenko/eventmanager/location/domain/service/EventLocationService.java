package ru.haritonenko.eventmanager.location.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.eventmanager.location.api.converter.EventLocationEntityConverter;
import ru.haritonenko.eventmanager.location.domain.EventLocation;
import ru.haritonenko.eventmanager.location.db.entity.EventLocationEntity;
import ru.haritonenko.eventmanager.location.api.exception.LocationNotFoundException;
import ru.haritonenko.eventmanager.location.api.filter.EventLocationSearchFilter;
import ru.haritonenko.eventmanager.location.db.repository.EventLocationRepository;

import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventLocationService {

    @Value("${app.location.default-page-size}")
    private int defaultPageSize;

    @Value("${app.location.default-page-number}")
    private int defaultPageNumber;
    private final EventLocationRepository locationRepository;
    private final EventLocationEntityConverter converter;

    public EventLocationService(EventLocationRepository locationRepository, EventLocationEntityConverter converter) {
        this.locationRepository = locationRepository;
        this.converter = converter;
    }

    public List<EventLocation> getAllLocations(
            EventLocationSearchFilter locationFilter
    ) {
        log.info("Searching all locations");
        int pageSize = Objects.nonNull(locationFilter.pageSize())
                ? locationFilter.pageSize() : defaultPageSize;
        int pageNumber = Objects.nonNull(locationFilter.pageNumber())
                ? locationFilter.pageNumber() : defaultPageNumber;

        Pageable pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);
        log.info("All locations were found");
        return locationRepository.searchBooks(
                        locationFilter.name(),
                        locationFilter.address(),
                        pageable
                )
                .stream()
                .map(converter::toDomain)
                .sorted(Comparator.comparing(EventLocation::id))
                .collect(Collectors.toList());
    }

    @Transactional
    public EventLocation createLocation(EventLocation eventLocationToCreate) {
        log.info("Creating a location");
        var newLocation = new EventLocationEntity(
                eventLocationToCreate.id(),
                eventLocationToCreate.name(),
                eventLocationToCreate.address(),
                eventLocationToCreate.capacity(),
                eventLocationToCreate.description()
        );
        var savedLocationEntity = locationRepository.save(newLocation);
        log.info("Location was successfully created");
        return converter.toDomain(savedLocationEntity);
    }

    @Transactional(readOnly = true)
    public EventLocation getLocationById(Integer id) {
        log.info("Getting location by id: {}", id);
        var foundLocation = locationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Error while searching for location by id: {}", id);
                    return new LocationNotFoundException(
                            "No found location by id = %s".formatted(id));
                });
        log.info("Location was successfully found by id: {}", id);
        return converter.toDomain(foundLocation);
    }

    public EventLocation updateLocation(Integer id, EventLocation eventLocationToUpdate) {
        log.info("Updating location with id: {}", id);
        if (!locationRepository.existsById(id)) {
            log.error("Error while checking location is existed by id: {}", id);
            throw new LocationNotFoundException(
                    "No found location by id = %s".formatted(id));
        }
        locationRepository.updateLocation(
                id,
                eventLocationToUpdate.name(),
                eventLocationToUpdate.address(),
                eventLocationToUpdate.capacity(),
                eventLocationToUpdate.description()
        );
        log.info("Location with id: {} was successfully updated", id);
        return converter.toDomain(
                locationRepository.findById(id).orElseThrow());
    }

    @Transactional
    public void deleteLocation(Integer id) {
        log.info("Deleting location by id: {}", id);
        if (!locationRepository.existsById(id)) {
            log.error("Error while finding location by id: {}", id);
            throw new LocationNotFoundException(
                    "No found location by id = %s".formatted(id));
        }
        locationRepository.deleteById(id);
        log.info("Location was successfully deleted by id: {}", id);
    }
}
