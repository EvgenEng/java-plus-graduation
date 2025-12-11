package ru.practicum.entities.request.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotNull(message = "Список идентификаторов запросов не может быть null")
    private List<Long> requestIds;

    @NotNull(message = "Новый статус запроса не может быть null")
    private String status;
}
