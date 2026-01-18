
package ru.practicum.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCompilationRequestDto {

    Boolean pinned;
    @Size(min = 1, max = 50, message = "Название подборки должно быть от 1 до 50 символов")
    String title;
    List<Long> events;

}
