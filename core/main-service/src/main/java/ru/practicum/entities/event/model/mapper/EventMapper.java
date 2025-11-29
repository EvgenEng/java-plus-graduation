package ru.practicum.entities.event.model.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.entities.category.model.Category;
import ru.practicum.entities.category.model.mapper.CategoryMapper;
import ru.practicum.entities.event.model.Event;
import ru.practicum.entities.event.model.Location;
import ru.practicum.entities.event.model.dto.EventDto;
import ru.practicum.entities.event.model.enums.EventState;
import ru.practicum.entities.user.model.User;
import ru.practicum.entities.user.model.mapper.UserMapper;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {
    public static EventDto toEventDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.categoryToDto(event.getCategory()).getId())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserDto(event.getInitiator()))
                .location(new Location(event.getLat(), event.getLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(event.getConfirmedRequests())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(0L)
                .build();
    }

    public static Event newRequestToEvent(EventDto eventDto, User user, Category category) {
        return Event.builder()
                .initiator(user)
                .category(category)
                .title(eventDto.getTitle())
                .paid(eventDto.getPaid() != null && eventDto.getPaid())
                .requestModeration(eventDto.getRequestModeration() == null || eventDto.getRequestModeration())
                .participantLimit(eventDto.getParticipantLimit() == null ? 0 : eventDto.getParticipantLimit())
                .lon(eventDto.getLocation().getLon())
                .lat(eventDto.getLocation().getLat())
                .annotation(eventDto.getAnnotation())
                .eventDate(eventDto.getEventDate())
                .description(eventDto.getDescription())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .confirmedRequests(0L)
                .build();
    }
}