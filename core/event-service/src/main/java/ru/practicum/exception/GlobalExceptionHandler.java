/*package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.dto.ErrorResponseDto;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("error")
                .error("Bad Request")
                .message(message)
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("error")
                .error("Bad Request")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("error")
                .error("Bad Request")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(Exception ex) {
        log.error("Internal server error", ex);

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("error")
                .error("Internal Server Error")
                .message("Произошла внутренняя ошибка сервера")
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("error")
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConditionsNotMetException.class)
    public ResponseEntity<ErrorResponseDto> handleConditionsNotMet(ConditionsNotMetException ex) {
        log.warn("Conditions not met: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("error")
                .error("Conflict")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.CONFLICT.value())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(ConflictException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("CONFLICT")
                .error("Conflict")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("error")
                .error("Forbidden")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }*/

    /*@ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage(), ex);

        if (ex instanceof IllegalArgumentException ||
                ex instanceof ConditionsNotMetException ||
                ex instanceof EntityNotFoundException) {
            throw ex;
        }

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("error")
                .error("Internal Server Error")
                .message("Произошла внутренняя ошибка сервера")
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }*/
    /*@ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage(), ex);

        // Возвращаем соответствующий статус
        if (ex instanceof IllegalArgumentException) {
            // Возвращаем 400, а не бросаем снова
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .status("error")
                    .error("Bad Request")
                    .message(ex.getMessage())
                    .timestamp(LocalDateTime.now())
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Для остальных RuntimeException возвращаем 500
        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("error")
                .error("Internal Server Error")
                .message("Произошла внутренняя ошибка сервера")
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}*/
package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.dto.ErrorResponseDto;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========== 400 BAD REQUEST (Валидация) ==========
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return buildErrorResponse("Bad Request", message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return buildErrorResponse("Bad Request", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return buildErrorResponse("Bad Request", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({DateValidationException.class})
    public ResponseEntity<ErrorResponseDto> handleDateValidation(DateValidationException ex) {
        log.warn("Date validation error: {}", ex.getMessage());
        return buildErrorResponse("Bad Request", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingParameter(MissingServletRequestParameterException ex) {
        String message = String.format("Missing parameter: '%s' of type '%s'",
                ex.getParameterName(), ex.getParameterType());
        log.warn("Missing parameter: {}", message);
        return buildErrorResponse("Bad Request", message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' should be of type '%s'",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Argument type mismatch: {}", message);
        return buildErrorResponse("Bad Request", message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return buildErrorResponse("Bad Request", "Malformed JSON request", HttpStatus.BAD_REQUEST);
    }

    // ========== 404 NOT FOUND ==========
    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<ErrorResponseDto> handleNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return buildErrorResponse("Not Found", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // ========== 409 CONFLICT ==========
    @ExceptionHandler({ConflictException.class, ConditionsNotMetException.class})
    public ResponseEntity<ErrorResponseDto> handleConflict(RuntimeException ex) {
        log.warn("Conflict detected: {}", ex.getMessage());
        return buildErrorResponse("Conflict", ex.getMessage(), HttpStatus.CONFLICT);
    }

    // ========== 403 FORBIDDEN ==========
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse("Forbidden", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // ========== 500 INTERNAL SERVER ERROR ==========
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(Exception ex) {
        log.error("Internal server error: ", ex); // Важно: логируем со стектрейсом
        return buildErrorResponse("Internal Server Error",
                "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЙ МЕТОД ==========
    private ResponseEntity<ErrorResponseDto> buildErrorResponse(String error, String message, HttpStatus status) {
        ErrorResponseDto response = ErrorResponseDto.builder()
                .status("error")
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .statusCode(status.value())
                .build();
        return ResponseEntity.status(status).body(response);
    }
}