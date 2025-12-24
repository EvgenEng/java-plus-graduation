package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.EventDto;
import ru.practicum.dto.EventInfoDto;
import ru.practicum.dto.Location;
import ru.practicum.dto.NewEventDto;
import ru.practicum.model.Event;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static EventDto toEventDto(Event event) {
        if (event == null) {
            return null;
        }

        try {
            return EventDto.builder()
                    .id(event.getId())
                    .annotation(event.getAnnotation() != null ? event.getAnnotation() : "")
                    .category(event.getCategoryId() != null ? event.getCategoryId() : 0L)
                    .createdOn(event.getCreatedOn() != null ? event.getCreatedOn() : LocalDateTime.now())
                    .description(event.getDescription() != null ? event.getDescription() : "")
                    .eventDate(event.getEventDate() != null ? event.getEventDate() : LocalDateTime.now())
                    .initiator(event.getInitiatorId() != null ? event.getInitiatorId() : 0L)
                    .location(event.getLat() != null && event.getLon() != null ?
                            new Location(event.getLat(), event.getLon()) : new Location(0.0, 0.0))
                    .paid(event.getPaid() != null ? event.getPaid() : false)
                    .participantLimit(event.getParticipantLimit() != null ? event.getParticipantLimit() : 0L)
                    .confirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L)
                    .publishedOn(event.getPublishedOn())
                    .requestModeration(event.getRequestModeration() != null ? event.getRequestModeration() : true)
                    .state(event.getState() != null ? event.getState() : "PENDING")
                    .title(event.getTitle() != null ? event.getTitle() : "")
                    .views(event.getViews() != null ? event.getViews() : 0L)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при преобразовании события в DTO", e);
        }
    }

    public static EventInfoDto toEventInfoDto(Event event) {
        return EventInfoDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .state(event.getState())
                .categoryId(event.getCategoryId())
                .initiatorId(event.getInitiatorId())
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(event.getConfirmedRequests())
                .paid(event.getPaid())
                .requestModeration(event.getRequestModeration())
                .build();
    }

    public static Event toEvent(EventDto eventDto, Long initiatorId) {
        return Event.builder()
                .initiatorId(initiatorId)
                .categoryId(eventDto.getCategory())
                .title(eventDto.getTitle())
                .paid(eventDto.getPaid() != null && eventDto.getPaid())
                .requestModeration(eventDto.getRequestModeration() == null || eventDto.getRequestModeration())
                .participantLimit(eventDto.getParticipantLimit() == null ? 0L : eventDto.getParticipantLimit())
                .lon(eventDto.getLocation().getLon())
                .lat(eventDto.getLocation().getLat())
                .annotation(eventDto.getAnnotation())
                .eventDate(eventDto.getEventDate())
                .description(eventDto.getDescription())
                .createdOn(LocalDateTime.now())
                .state("PENDING")
                .confirmedRequests(0L)
                .views(0L)
                .build();
    }

    public static Event toEvent(NewEventDto newEventDto, Long initiatorId) {
        return Event.builder()
                .initiatorId(initiatorId)
                .categoryId(newEventDto.getCategory())
                .title(newEventDto.getTitle())
                .paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false)
                .requestModeration(newEventDto.getRequestModeration() != null ?
                        newEventDto.getRequestModeration() : true)
                .participantLimit(newEventDto.getParticipantLimit() != null ?
                        newEventDto.getParticipantLimit() : 0L)
                .lon(newEventDto.getLocation().getLon())
                .lat(newEventDto.getLocation().getLat())
                .annotation(newEventDto.getAnnotation())
                .eventDate(newEventDto.getEventDate())
                .description(newEventDto.getDescription())
                .createdOn(LocalDateTime.now())
                .state("PENDING")
                .confirmedRequests(0L)
                .views(0L)
                .build();
    }
}
