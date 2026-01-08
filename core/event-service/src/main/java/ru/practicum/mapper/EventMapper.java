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

    /*    public static EventDto toEventDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(event.getCategoryId())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(event.getInitiatorId())
                .location(new Location(event.getLat(), event.getLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(event.getConfirmedRequests())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }*/
    public static EventDto toEventDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(event.getCategoryId())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(event.getInitiatorId())
                .location(event.getLat() != null && event.getLon() != null
                        ? new Location(event.getLat(), event.getLon())
                        : null)
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(event.getConfirmedRequests() != null
                        ? event.getConfirmedRequests() : 0L)
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews() != null ? event.getViews() : 0L)
                .build();
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
