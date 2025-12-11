package ru.practicum.entities.event.model.dto;

import ru.practicum.dto.Location;
import java.time.LocalDateTime;

public interface UpdateEventBaseDto {
    String getAnnotation();

    String getDescription();

    LocalDateTime getEventDate();

    Boolean getPaid();

    Long getParticipantLimit();

    Boolean getRequestModeration();

    String getTitle();

    Location getLocation();
}
