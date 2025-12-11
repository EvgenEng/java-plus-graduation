package ru.practicum.entities.event.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.practicum.dto.Location;
import ru.practicum.utils.DateTimeConstants;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminEventDto implements UpdateEventBaseDto {
    @Length(min = 20, max = 2000)
    private String annotation;

    @Length(min = 20, max = 7000)
    private String description;

    @Positive
    private Long category;

    @Future
    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @PositiveOrZero
    private Long participantLimit;

    private Boolean requestModeration;

    @Length(min = 3, max = 120)
    private String title;

    private String stateAction;
}
