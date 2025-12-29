package ru.haritonenko.eventmanager.web.errorDto;

public record ErrorMessageResponse(
        String message,
        String detailedMessage,
        String dateTime
) {
}
