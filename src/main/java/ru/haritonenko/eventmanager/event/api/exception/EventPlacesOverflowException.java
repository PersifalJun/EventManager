package ru.haritonenko.eventmanager.event.api.exception;

public class EventPlacesOverflowException extends RuntimeException {
    public EventPlacesOverflowException(String message) {
        super(message);
    }
}
