package ru.haritonenko.eventmanager.user.api.exception;

public class UserBookedEventException extends RuntimeException {
    public UserBookedEventException(String message) {
        super(message);
    }
}
