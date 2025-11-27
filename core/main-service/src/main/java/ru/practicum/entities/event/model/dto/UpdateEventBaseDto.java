package ru.practicum.entities.event.model.dto;

import ru.practicum.entities.event.model.Location;

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