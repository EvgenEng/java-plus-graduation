package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;

    private Long userId;

    private Long eventId;

    @NotBlank(message = "Комментарий не должен быть пустым")
    @Size(min = 3, max = 2000, message = "Размер комментария должен быть от 3 до 2000 символов")
    private String message;

    private LocalDateTime created;
}
