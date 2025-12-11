package ru.practicum.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.DateValidationException;
import ru.practicum.exception.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("Entity not found: {}", e.getMessage());
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.NOT_FOUND.toString())
                .reason("Запрошенный объект не найден.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionsNotMetException(ConditionsNotMetException e) {
        log.error("Conditions not met: {}", e.getMessage());
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.CONFLICT.toString())
                .reason("Нарушены условия целостности данных.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage());
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.FORBIDDEN.toString())
                .reason("Недостаточно прав для выполнения операции.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDateValidationException(DateValidationException e) {
        log.error("Date validation error: {}", e.getMessage());
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Нарушены условия валидации дат.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGenericException(Exception e) {
        log.error("Internal server error: {}", e.getMessage(), e);
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .reason("Внутренняя ошибка сервера.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
