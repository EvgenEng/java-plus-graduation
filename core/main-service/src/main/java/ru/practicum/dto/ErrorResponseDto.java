package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    private String status;       // "CONFLICT", "NOT_FOUND", "BAD_REQUEST"
    private String error;        // "Conflict", "Not Found"
    private String message;      // Сообщение об ошибке
    private String reason;       // Причина

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private Integer statusCode;  // HTTP статус код: 409, 404, 400
}
