package ru.practicum.exception.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    // 400 - Валидация DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidRequest(final MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.toList());

        log.warn("Validation error: {}", errors);
        return ApiError.builder()
                .errors(errors)
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Incorrect request")
                .message("Validation error")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 400 - Валидация параметров запроса
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationException(final ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        log.warn("Constraint violation: {}", errors);
        return ApiError.builder()
                .errors(errors)
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Validation error")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 400 - Неправильный тип параметра
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String error = String.format("Parameter '%s' should be of type %s",
                e.getName(), e.getRequiredType().getSimpleName());

        log.warn("Type mismatch: {}", error);
        return ApiError.builder()
                .errors(List.of(error))
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Incorrect parameter type")
                .message(error)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 400 - Отсутствует обязательный параметр
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestParameterException(final MissingServletRequestParameterException e) {
        String error = "Required parameter is missing: " + e.getParameterName();

        log.warn("Missing parameter: {}", error);
        return ApiError.builder()
                .errors(List.of(error))
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Required parameter missing")
                .message(error)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 400 - Нечитаемый JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMessageNotReadableException(final HttpMessageNotReadableException e) {
        log.warn("Message not readable: {}", e.getMessage());
        return ApiError.builder()
                .errors(List.of(e.getMessage()))
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Invalid request format")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 409 - Конфликт данных (дублирование запроса)
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String message = "Data integrity violation";
        String reason = "Data conflict";

        if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
            message = "Duplicate request";
            reason = "Request from this user for this event already exists";
        }

        log.warn("Data integrity violation: {}", e.getMessage());
        return ApiError.builder()
                .errors(List.of(e.getMostSpecificCause().getMessage()))
                .status(HttpStatus.CONFLICT.toString())
                .reason(reason)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 404 - Запрос не найден
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("Entity not found: {}", e.getMessage());
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.NOT_FOUND.toString())
                .reason("Request not found")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 403 - Доступ запрещен
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage());
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.FORBIDDEN.toString())
                .reason("Access denied")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 409 - Условия не выполнены
    @ExceptionHandler(ConditionsNotMetException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionsNotMetException(ConditionsNotMetException e) {
        log.error("Conditions not met: {}", e.getMessage());
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.CONFLICT.toString())
                .reason("Conditions not met")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 500 - Все остальные исключения
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGenericException(Exception e) {
        log.error("Internal server error: {}", e.getMessage(), e);
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .reason("Internal server error")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
