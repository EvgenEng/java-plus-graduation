package ru.practicum.entities.request.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    @NotNull(message = "Дата создания заявки не может быть null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime created;

    @NotNull(message = "Идентификатор события не может быть null")
    private Long event; // Идентификатор события

    @NotNull(message = "Идентификатор заявки не может быть null")
    private Long id;

    @NotNull(message = "Идентификатор пользователя не может быть null")
    private Long requester;

    @NotNull(message = "Статус заявки не может быть null")
    private String status;
}
