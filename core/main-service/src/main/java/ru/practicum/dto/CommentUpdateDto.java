package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateDto {
    @NotBlank(message = "Текст комментария не должен быть пустым")
    @Size(min = 1, max = 2000, message = "Комментарий должен содержать от 1 до 2000 символов")
    private String text;
}
