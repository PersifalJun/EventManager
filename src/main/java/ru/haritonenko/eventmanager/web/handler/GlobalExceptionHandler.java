package ru.haritonenko.eventmanager.web.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.haritonenko.eventmanager.location.api.exception.LocationNotFoundException;
import ru.haritonenko.eventmanager.user.api.exception.UserAlreadyRegisteredException;
import ru.haritonenko.eventmanager.user.api.exception.UserNotFoundException;
import ru.haritonenko.eventmanager.web.errorDto.ErrorMessageResponse;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Got validation exception", ex);
        String detailedMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> (error.getField() + ": " + error.getDefaultMessage()))
                .collect(Collectors.joining(","));
        var errorDto = getErrorMessageResponse("Validation Error",
                detailedMessage);

        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleNoFoundLocationException(
            LocationNotFoundException ex
    ) {
        log.error("Got NoFoundLocationException", ex);
        var errorDto = getErrorMessageResponse("Location search error",
                ex.getMessage());
        return ResponseEntity.
                status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleNoFoundUserException(
            UserNotFoundException ex
    ) {
        log.error("Got NoFoundUserException", ex);
        var errorDto = getErrorMessageResponse("User search error",
                ex.getMessage());
        return ResponseEntity.
                status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    public ResponseEntity<ErrorMessageResponse> handleUserAlreadyRegisteredException(
            UserAlreadyRegisteredException ex
    ) {
        log.error("Got AlreadyRegisteredUserException", ex);
        var errorDto = getErrorMessageResponse("User registration error",
                ex.getMessage());
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorMessageResponse> handleAuthorizationDeniedException(
            AuthorizationDeniedException ex
    ) {
        log.error("Got AuthorizationDeniedException", ex);
        var errorDto = getErrorMessageResponse("Forbidden",
                ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorDto);
    }

    private ErrorMessageResponse getErrorMessageResponse(
            String message,
            String detailedMessage
    ) {
        return new ErrorMessageResponse(
                message,
                detailedMessage,
                LocalDateTime.now().toString()
        );
    }
}
