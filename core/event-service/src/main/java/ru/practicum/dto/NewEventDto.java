package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank(message = "Аннотация не может быть пустой.")
    @Size(min = 20, max = 2000, message = "Длина аннотации должна быть от 20 до 2000 символов.")
    private String annotation;

    @NotNull(message = "Категория не может быть пустой.")
    private Long category;

    @NotBlank(message = "Описание не может быть пустым.")
    @Size(min = 20, max = 7000, message = "Длина описания должна быть от 20 до 7000 символов.")
    private String description;

    @NotNull(message = "Дата события не может быть пустой.")
    @Future(message = "Дата события должна быть в будущем.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Местоположение не может быть пустым.")
    @Valid
    private Location location;

    @Builder.Default
    private Boolean paid = false;

    @Min(value = 0, message = "Лимит участников не может быть отрицательным.")
    @Builder.Default
    private Long participantLimit = 0L;

    @Builder.Default
    private Boolean requestModeration = true;

    @NotBlank(message = "Заголовок не может быть пустым.")
    @Size(min = 3, max = 120, message = "Длина заголовка должна быть от 3 до 120 символов.")
    private String title;
}
