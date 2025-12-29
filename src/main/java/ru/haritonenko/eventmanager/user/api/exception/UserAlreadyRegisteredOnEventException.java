package ru.haritonenko.eventmanager.user.api.exception;

public class UserAlreadyRegisteredOnEventException extends RuntimeException {
    public UserAlreadyRegisteredOnEventException(String message) {
        super(message);
    }
}
