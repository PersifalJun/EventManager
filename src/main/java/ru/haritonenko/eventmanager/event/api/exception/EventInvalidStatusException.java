package ru.haritonenko.eventmanager.event.api.exception;

public class EventInvalidStatusException extends RuntimeException {
    public EventInvalidStatusException(String message) {
        super(message);
    }
}
