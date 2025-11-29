package ru.practicum.entities.comment.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.entities.event.model.dto.EventDto;
import ru.practicum.entities.user.model.dto.UserDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;

    private UserDto user;

    private EventDto event;

    @NotBlank(message = "Комментарий не должен быть пустым")
    @Size(min = 3, max = 2000, message = "Размер комментария должен быть от 3 до 2000 символов")
    private String message;

    private LocalDateTime created;

    private Long views;
}