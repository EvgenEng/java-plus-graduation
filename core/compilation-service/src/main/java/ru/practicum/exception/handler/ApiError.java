package ru.practicum.exception.handler;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApiError {
    private List<String> errors;
    private String status;
    private String reason;
    private String message;
    private String timestamp;
}
