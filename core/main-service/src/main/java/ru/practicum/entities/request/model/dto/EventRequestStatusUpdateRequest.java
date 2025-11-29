package ru.practicum.entities.request.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.entities.request.model.ParticipationRequestStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotNull(message = "Список идентификаторов запросов не может быть null")
    private List<Long> requestIds; // Список идентификаторов запросов на участие

    @NotNull(message = "Новый статус запроса не может быть null")
    private ParticipationRequestStatus status; // Новый статус запроса (CONFIRMED или REJECTED)
}