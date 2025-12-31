package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatusUpdateDto {
    @NotNull(message = "requestIds не может быть null")
    private List<Long> requestIds;

    @NotNull(message = "status не может быть null")
    private String status; // "CONFIRMED" или "REJECTED"
}
