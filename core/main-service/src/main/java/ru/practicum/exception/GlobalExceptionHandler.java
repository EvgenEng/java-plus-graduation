package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.dto.ErrorResponseDto;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Конфликты (409) - САМЫЙ ВАЖНЫЙ!
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(ConflictException ex) {
        log.warn("Конфликт: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("CONFLICT")
                .error("Conflict")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.CONFLICT.value())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 2. Сущность не найдена (404)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Сущность не найдена: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("NOT_FOUND")
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 3. Условия не выполнены (409 или 403)
    @ExceptionHandler(ConditionsNotMetException.class)
    public ResponseEntity<ErrorResponseDto> handleConditionsNotMet(ConditionsNotMetException ex) {
        log.warn("Условия не выполнены: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("FORBIDDEN")
                .error("Forbidden")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // 4. Неверные аргументы (400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Неверный аргумент: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("BAD_REQUEST")
                .error("Bad Request")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 5. Все остальные исключения (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(Exception ex) {
        log.error("Внутренняя ошибка сервера", ex);

        ErrorResponseDto error = ErrorResponseDto.builder()
                .status("INTERNAL_SERVER_ERROR")
                .error("Internal Server Error")
                .message("Произошла внутренняя ошибка сервера")
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
