package ru.haritonenko.eventmanager.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.haritonenko.eventmanager.event.api.converter.EventEntityConverter;
import ru.haritonenko.eventmanager.event.api.dto.EventCreateRequestDto;
import ru.haritonenko.eventmanager.event.api.dto.EventUpdateRequestDto;
import ru.haritonenko.eventmanager.event.api.dto.filter.EventPageFilter;
import ru.haritonenko.eventmanager.event.api.dto.filter.EventSearchRequestDto;
import ru.haritonenko.eventmanager.event.api.exception.EventCountPlacesException;
import ru.haritonenko.eventmanager.event.api.exception.EventInvalidStatusException;
import ru.haritonenko.eventmanager.event.api.exception.EventNotFoundException;
import ru.haritonenko.eventmanager.event.api.exception.EventPlacesOverflowException;
import ru.haritonenko.eventmanager.event.registration.db.EventRegistrationRepository;
import ru.haritonenko.eventmanager.event.registration.db.entity.EventRegistrationEntity;
import ru.haritonenko.eventmanager.event.registration.status.EventRegistrationStatus;
import ru.haritonenko.eventmanager.location.api.exception.LocationNotFoundException;
import ru.haritonenko.eventmanager.location.db.entity.EventLocationEntity;
import ru.haritonenko.eventmanager.user.api.exception.UserAlreadyRegisteredOnEventException;
import ru.haritonenko.eventmanager.event.api.status.EventStatus;
import ru.haritonenko.eventmanager.event.db.entity.EventEntity;
import ru.haritonenko.eventmanager.event.db.repository.EventRepository;
import ru.haritonenko.eventmanager.event.service.domain.Event;
import ru.haritonenko.eventmanager.location.db.repository.EventLocationRepository;
import ru.haritonenko.eventmanager.user.api.exception.UserBookedEventException;
import ru.haritonenko.eventmanager.user.api.exception.UserNotFoundException;
import ru.haritonenko.eventmanager.user.api.role.UserRole;
import ru.haritonenko.eventmanager.user.db.entity.UserEntity;
import ru.haritonenko.eventmanager.user.db.repository.UserRepository;
import ru.haritonenko.eventmanager.user.service.domain.User;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    @Value("${app.location.default-page-size}")
    private int defaultPageSize;

    @Value("${app.location.default-page-number}")
    private int defaultPageNumber;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventLocationRepository eventLocationRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventEntityConverter converter;

    @Transactional(readOnly = true)
    public Event getEventById(Integer id) {
        log.info("Getting event by id: {}", id);
        var foundEvent = getEventByIdOrThrow(id);
        log.info("Event was successfully found by id: {}", id);
        return converter.toDomain(foundEvent);
    }

    @Transactional
    public Event createEventByUserId(
            Integer ownerId,
            EventCreateRequestDto eventToCreate
    ) {
        log.info("Creating an event");
        var owner = getUserByIdOrThrow(ownerId);
        var locationId = eventToCreate.locationId();
        var location = getEventLocationByIdOrThrow(locationId);
        checkLocationCapacityIsMoreOrEqualsEventPlacesOrThrow(location, eventToCreate.maxPlaces());
        var newEvent = new EventEntity(
                null,
                eventToCreate.name(),
                owner,
                location,
                new ArrayList<>(),
                eventToCreate.maxPlaces(),
                0,
                eventToCreate.date(),
                BigDecimal.valueOf(eventToCreate.cost()),
                eventToCreate.duration(),
                EventStatus.WAIT_START
        );
        location.addEvent(newEvent);
        owner.addOwnEvent(newEvent);
        var savedEventEntity = eventRepository.save(newEvent);
        log.info("Event was successfully created");
        return converter.toDomain(savedEventEntity);
    }

    @Transactional
    public Event updateEvent(
            Integer ownerId,
            Integer eventId,
            EventUpdateRequestDto eventToUpdate
    ) {
        log.info("Updating event with id: {}", eventId);

        var user = getUserByIdOrThrow(ownerId);
        var event = getEventByIdOrThrow(eventId);

        checkUserOrAdminRoleToUpdateOrDelete(user, event);

        var newLocation = getEventLocationByIdOrThrow(eventToUpdate.locationId());
        var oldLocation = event.getLocation();

        checkLocationCapacityIsMoreOrEqualsEventPlacesOrThrow(newLocation, eventToUpdate.maxPlaces());
        checkCountOfOccupiedPlacesLessThanMax(eventToUpdate, event);

        if (!oldLocation.getId().equals(newLocation.getId())) {
            oldLocation.getEvents().remove(event);
            newLocation.getEvents().add(event);
            event.setLocation(newLocation);
        }
        event.setName(eventToUpdate.name());
        event.setMaxPlaces(eventToUpdate.maxPlaces());
        event.setDate(eventToUpdate.date());
        event.setCost(eventToUpdate.cost());
        event.setDuration(eventToUpdate.duration());
        event.setLocation(newLocation);

        log.info("Event with id: {} was successfully updated", eventId);
        return converter.toDomain(event);
    }

    public List<Event> findEventsCreatedByUser(
            User user,
            EventPageFilter pageFilter
    ) {
        log.info("Searching all user's created events");
        checkUserRoleToGetListOfOwnEvents(user.id());
        var createdEvents = eventRepository.searchCreatedEventsByUserId(
                user.id(),
                getPageable(pageFilter)
        );
        return getSortedEventListByEventId(createdEvents);
    }

    public List<Event> findEventsBookedByUser(
            User user,
            EventPageFilter pageFilter
    ) {
        log.info("Searching all user's booked events");
        var bookedEvents = eventRepository.searchBookedEventsByUserId(
                user.id(),
                getPageable(pageFilter)
        );
        return getSortedEventListByEventId(bookedEvents);
    }

    public List<Event> searchEventWithFilter(
            EventSearchRequestDto eventFilter,
            EventPageFilter pageFilter
    ) {
        log.info("Searching events with filter");

        BigDecimal costMin = isNull(eventFilter.costMin()) ? null : BigDecimal.valueOf(eventFilter.costMin().doubleValue());
        BigDecimal costMax = isNull(eventFilter.costMax()) ? null : BigDecimal.valueOf(eventFilter.costMax().doubleValue());

        checkFilterConstraintsOrThrow(
                costMin,
                costMax,
                eventFilter.placesMin(),
                eventFilter.placesMax(),
                eventFilter.dateStartAfter(),
                eventFilter.dateStartBefore(),
                eventFilter.durationMin(),
                eventFilter.durationMax()
        );
        var foundEventsWithFilter = eventRepository.searchEventsWithFilter(
                eventFilter.name(),
                eventFilter.placesMin(),
                eventFilter.placesMax(),
                eventFilter.dateStartAfter(),
                eventFilter.dateStartBefore(),
                costMin,
                costMax,
                eventFilter.durationMin(),
                eventFilter.durationMax(),
                eventFilter.locationId(),
                eventFilter.eventStatus(),
                getPageable(pageFilter)
        );
        return getSortedEventListByEventId(foundEventsWithFilter);
    }

    @Transactional
    public Event registerOnEvent(Integer userId, Integer eventId) {
        log.info("Registration user on event");
        var user = getUserByIdOrThrow(userId);
        var event = getEventByIdOrThrow(eventId);

        log.info("Checking registry conditions");
        checkEventCreatorIsNotMemberOrThrow(event, userId);
        checkEventStatusWhileRegistrationOrThrow(event);

        var regOpt = eventRegistrationRepository.findByUserIdAndEventId(userId, eventId);

        if (regOpt.isPresent()) {
            var reg = regOpt.get();

            if (reg.getStatus() == EventRegistrationStatus.ACTIVE) {
                throw new UserAlreadyRegisteredOnEventException("You have already registered on this event");
            }

            int updated = eventRepository.incOccupiedPlaces(eventId);
            if (updated == 0) {
                log.error("Error while booking event place");
                throw new EventPlacesOverflowException("Places are overflowed");
            }

            eventRegistrationRepository.updateStatus(userId, eventId, EventRegistrationStatus.ACTIVE);

            var updatedEvent = getEventByIdOrThrow(eventId);
            return converter.toDomain(updatedEvent);
        }

        int updated = eventRepository.incOccupiedPlaces(eventId);
        if (updated == 0) {
            log.error("Error while booking event place");
            throw new EventPlacesOverflowException("Places are overflowed");
        }

        var registration = new EventRegistrationEntity(
                null,
                user,
                event,
                EventRegistrationStatus.ACTIVE
        );
        eventRegistrationRepository.save(registration);

        var updatedEvent = getEventByIdOrThrow(eventId);
        return converter.toDomain(updatedEvent);
    }

    @Transactional
    public void deleteEventById(Integer ownerId, Integer eventId) {

        log.info("Deleting event by id: {}", eventId);
        var event = getEventByIdOrThrow(eventId);
        var user = getUserByIdOrThrow(ownerId);

        log.info("Checking conditions before deleting event");
        checkUserOrAdminRoleToUpdateOrDelete(user, event);
        checkEventStatusWhileDeletingEventOrRegistrationRequestByIdOrThrow(event);
        event.setStatus(EventStatus.CANCELLED);
    }

    @Transactional
    public void cancelEventRegistrationRequestById(Integer userId, Integer eventId) {
        log.info("Cancelling event registration by id: {}", eventId);

        var event = getEventByIdOrThrow(eventId);
        checkEventStatusWhileDeletingEventOrRegistrationRequestByIdOrThrow(event);

        var reg = eventRegistrationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new EventNotFoundException("Registration not found"));

        if (reg.getStatus() != EventRegistrationStatus.ACTIVE) {
            throw new EventNotFoundException("Active registration not found");
        }

        int updated = eventRepository.decOccupiedPlaces(eventId);
        if (updated == 0) {
            throw new EventCountPlacesException("Places quantity can`t be less than zero");
        }

        eventRegistrationRepository.updateStatus(userId, eventId, EventRegistrationStatus.CANCELLED);
    }

    private void checkFilterConstraintsOrThrow(
            BigDecimal costMin,
            BigDecimal costMax,
            Integer placesMin,
            Integer placesMax,
            String dateStartAfter,
            String dateStartBefore,
            Integer durationMin,
            Integer durationMax

    ) {
        if (nonNull(placesMin) && nonNull(placesMax)
                && placesMin > placesMax) {
            log.error("Error while checking event places");
            throw new IllegalArgumentException("placesMin can not be more than placesMax");
        }
        if (nonNull(durationMin)
                && nonNull(durationMax) && durationMin > durationMax) {
            log.error("Error while checking event duration");
            throw new IllegalArgumentException("durationMin can not be more than durationMax");
        }
        if (nonNull(costMin) && nonNull(costMax)
                && costMin.compareTo(costMax) > 0) {
            log.error("Error while checking event cost");
            throw new IllegalArgumentException("costMin can not be  more than costMax");
        }
        if (nonNull(dateStartAfter) && nonNull(dateStartBefore)
                && dateStartAfter.compareTo(dateStartBefore) > 0) {
            log.error("Error while checking event date");
            throw new IllegalArgumentException("dateStartAfter can not be later than dateStartBefore");
        }
    }

    private UserEntity getUserByIdOrThrow(Integer ownerId) {
        return userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.error("Error while finding user by id: {}", ownerId);
                    return new UserNotFoundException("User not found");
                });
    }

    private EventEntity getEventByIdOrThrow(Integer eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Error while searching for event by id: {}", eventId);
                    return new EventNotFoundException(
                            "No found event by id = %s".formatted(eventId));
                });
    }

    private EventLocationEntity getEventLocationByIdOrThrow(
            Integer locationId
    ) {
        return eventLocationRepository.findById(locationId)
                .orElseThrow(() -> {
                    log.error("Error while searching for event location by id: {}", locationId);
                    return new LocationNotFoundException(
                            "No found event location by id = %s".formatted(locationId));
                });
    }

    private Pageable getPageable(EventPageFilter pageFilter) {
        int pageSize = Objects.nonNull(pageFilter.pageSize())
                ? pageFilter.pageSize() : defaultPageSize;
        int pageNumber = Objects.nonNull(pageFilter.pageNumber())
                ? pageFilter.pageNumber() : defaultPageNumber;
        return Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);
    }

    private List<Event> getSortedEventListByEventId(List<EventEntity> events) {
        return events.stream()
                .map(converter::toDomain)
                .sorted(Comparator.comparing(Event::id))
                .collect(Collectors.toList());
    }

    private void checkEventStatusWhileRegistrationOrThrow(
            EventEntity eventToBeBookedByUser
    ) {
        if (eventToBeBookedByUser.getStatus() == EventStatus.CANCELLED
                || eventToBeBookedByUser.getStatus() == EventStatus.FINISHED) {
            log.error("Error while checking event status");
            throw new EventInvalidStatusException("This event has already cancelled or finished");
        }
        if (eventToBeBookedByUser.getStatus() == EventStatus.STARTED) {
            log.error("Error while checking event status");
            throw new EventInvalidStatusException("You can`t register,because this event has already started");
        }
    }

    public void checkEventStatusWhileDeletingEventOrRegistrationRequestByIdOrThrow(
            EventEntity eventToDeleteByOwner
    ) {
        if (eventToDeleteByOwner.getStatus() != EventStatus.WAIT_START) {
            log.error("Error while checking event status to delete event or registration request");
            throw new EventInvalidStatusException("You can`t delete event or registration request " +
                    "when it has already started, cancelled or finished");
        }
    }

    private void checkEventCreatorIsNotMemberOrThrow(
            EventEntity eventToBeBookedByUser,
            Integer userId
    ) {
        if (Objects.equals(eventToBeBookedByUser.getOwner().getId(), userId)) {
            log.error("Error while checking event creator");
            throw new UserAlreadyRegisteredOnEventException("Event creator is member by default");
        }
    }

    private void checkLocationCapacityIsMoreOrEqualsEventPlacesOrThrow(
            EventLocationEntity location,
            Integer eventPlaces
    ) {
        if (location.getCapacity() < eventPlaces) {
            log.error("Error while matching location and event places count");
            throw new EventCountPlacesException("Location capacity is less than event maxPlaces." +
                    " Chose new location or decrease quantity of event places.");
        }
    }

//    private void checkUserIsNotAlreadyRegisteredOnEventOrThrow(
//            EventEntity eventToBeBookedByUser,
//            UserEntity userToBeRegisteredOn
//    ) {
//        if (userToBeRegisteredOn.getBookedEvents().contains(eventToBeBookedByUser)) {
//            log.error("Error while checking event members");
//            throw new UserAlreadyRegisteredOnEventException("You have already registered on this event");
//        }
//    }

    private void checkUserOrAdminRoleToUpdateOrDelete(
            UserEntity user,
            EventEntity event
    ) {
        if (user.getUserRole() != UserRole.ADMIN
                && !event.getOwner().getId().equals(user.getId())) {
            log.error("Error while checking user and admin role");
            throw new AccessDeniedException("You are not owner of this event");
        }
    }

    private void checkUserRoleToGetListOfOwnEvents(
            Integer userId
    ) {
        var user = getUserByIdOrThrow(userId);
        if (user.getUserRole() != UserRole.USER) {
            log.error("Error while checking user role");
            throw new AccessDeniedException("You are not owner of this event");
        }
    }

    private void checkCountOfOccupiedPlacesLessThanMax(
            EventUpdateRequestDto eventToUpdate,
            EventEntity event
    ) {
        if (eventToUpdate.maxPlaces() < event.getOccupiedPlaces()) {
            log.error("Error while checking count of places");
            throw new EventCountPlacesException("Occupied places can't be more than event maxPlaces ");
        }
    }

//    private void checkUserBookedEventsListContainsEventOrThrow(
//            UserEntity user,
//            EventEntity event
//    ) {
//        if (!user.getBookedEvents().contains(event)) {
//            log.error("Error while cancelling registry request");
//            throw new UserBookedEventException("This event isn't booked by this user");
//        }
//    }
}

