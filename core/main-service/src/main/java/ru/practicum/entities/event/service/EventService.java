package ru.practicum.entities.event.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.centralRepository.CategoryRepository;
import ru.practicum.centralRepository.CommentRepository;
import ru.practicum.centralRepository.EventRepository;
import ru.practicum.centralRepository.UserRepository;
import ru.practicum.client.StatClient;
import ru.practicum.dto.StatsDto;
import ru.practicum.entities.category.model.Category;
import ru.practicum.entities.event.model.Event;
import ru.practicum.entities.event.model.dto.*;
import ru.practicum.entities.event.model.enums.EventAdminStateAction;
import ru.practicum.entities.event.model.enums.EventState;
import ru.practicum.entities.event.model.enums.EventUserStateAction;
import ru.practicum.entities.event.model.mapper.EventMapper;
import ru.practicum.entities.user.model.User;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.DateValidationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.utils.DateTimeConstants;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final StatClient statsClient;
    private final CommentRepository commentRepository;

    public List<EventDto> findByUserId(Long userId, Integer from, Integer size) {
        return eventRepository.findAllByInitiatorIdOrderByEventDateDesc(userId, from, size)
                .stream()
                .map(EventMapper::toEventDto)
                .toList();
    }

    public EventDto findByIdAndUser(Long userId, Long eventId) {
        userRepository.findById(userId);
        Event event = findEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Просмотр полной информации о событии доступен только для создателя события");
        }

        return EventMapper.toEventDto(event);
    }

    public List<EventDto> searchCommon(PublicEventSearch search) {
        if (search.getRangeEnd() != null && search.getRangeStart() != null &&
                search.getRangeEnd().isBefore(search.getRangeStart())) {
            throw new DateValidationException("Дата начала не должна быть позже даты окончания");
        }

        List<Event> events = eventRepository.findCommonEventsByFilters(search);

        Map<Long, Long> commentsCountMap;
        if (!events.isEmpty()) {
            List<Long> eventIds = events.stream().map(Event::getId).toList();
            // Вызываем новый метод репозитория
            List<Object[]> results = commentRepository.countByEventIdsGrouped(eventIds);
            // Преобразуем List<Object[]> в Map<Long, Long>
            commentsCountMap = results.stream()
                    .collect(Collectors.toMap(
                            result -> (Long) result[0], // eventId
                            result -> (Long) result[1]  // count
                    ));
        } else {
            commentsCountMap = Collections.emptyMap(); // Пустая карта, если событий нет
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        LocalDateTime now = LocalDateTime.now();
        List<StatsDto> stats = statsClient.getStats(
                DateTimeConstants.toString(LocalDateTime.of(1900, 1, 1, 0, 0)),
                DateTimeConstants.toString(now.plusMinutes(2)),
                uris,
                true
        );

        Map<String, Long> statsMap = stats.stream()
                .collect(Collectors.toMap(
                        StatsDto::getUri,
                        StatsDto::getHits
                ));

        return events.stream()
                .map(event -> {
                    EventDto dto = EventMapper.toEventDto(event);
                    // Установка просмотров
                    Long views = statsMap.getOrDefault("/events/" + event.getId(), 0L);
                    dto.setViews(views);
                    // Установка количества комментариев
                    Long commentsCount = commentsCountMap.getOrDefault(event.getId(), 0L);
                    dto.setCommentsCount(commentsCount);
                    return dto;
                })
                .toList();
    }

    @Transactional
    public List<EventDto> searchAdmin(AdminEventSearch search) {
        List<Event> events = eventRepository.findAdminEventsByFilters(search);
        return events.stream()
                .map(EventMapper::toEventDto)
                .toList();
    }

    public EventDto findById(Long eventId) {
        Event event = findEventById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        EventDto dto = EventMapper.toEventDto(event);

        Long views = getViews(eventId);
        dto.setViews(views);

        return dto;
    }

    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    @Transactional
    public EventDto create(Long userId, EventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + newEventDto.getCategory() + " не найдена"));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DateValidationException("Дата начала события должна быть не ранее чем через 2 часа от даты создания.");
        }

        return EventMapper.toEventDto(
                eventRepository.save(
                        EventMapper.newRequestToEvent(newEventDto, initiator, category)
                )
        );
    }

    @Transactional
    public EventDto updateByAdmin(long eventId, UpdateAdminEventDto eventDto) {
        Event event = findEventById(eventId);

        LocalDateTime eventDate = eventDto.getEventDate() == null ? event.getEventDate() : eventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new DateValidationException("Дата начала события должна быть не ранее чем через 1 час от даты редактирования.");
        }

        if (event.getState() == EventState.PUBLISHED && eventDto.getStateAction() == EventAdminStateAction.REJECT_EVENT) {
            throw new ConditionsNotMetException("Опубликованное событие нельзя отклонить.");
        }
        if (event.getState() != EventState.PENDING && eventDto.getStateAction() == EventAdminStateAction.PUBLISH_EVENT) {
            throw new ConditionsNotMetException("Опубликовать можно только событие в состоянии ожидания.");
        }

        if (eventDto.getCategory() != null) {
            Category category = categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + eventDto.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        updateEventFields(event, eventDto, eventDate);

        if (eventDto.getStateAction() != null) {
            event.setState(
                    eventDto.getStateAction() == EventAdminStateAction.PUBLISH_EVENT ? EventState.PUBLISHED : EventState.CANCELED
            );
        }

        return EventMapper.toEventDto(eventRepository.save(event));
    }

    @Transactional
    public EventDto updateByUser(Long userId, Long eventId, UpdateEventDto eventDto) {
        userRepository.findById(userId); // Проверка существования пользователя

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Событие может редактировать только его создатель");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Нельзя редактировать опубликованное событие");
        }

        LocalDateTime eventDate = eventDto.getEventDate() == null ? event.getEventDate() : eventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new DateValidationException("Дата начала события должна быть не ранее чем через 1 час от даты редактирования.");
        }

        if (eventDto.getCategory() != null) {
            Category category = categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + eventDto.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        if (eventDto.getStateAction() == EventUserStateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        } else if (eventDto.getStateAction() == EventUserStateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }

        updateEventFields(event, eventDto, eventDate);

        return EventMapper.toEventDto(eventRepository.save(event));
    }

    private Long getViews(Long id) {
        List<StatsDto> result = statsClient.getStats("1900-01-01 00:00:00",
                DateTimeConstants.toString(LocalDateTime.now().plusMinutes(2)),
                List.of("/events/" + id),
                true);

        return result.isEmpty() ? 0L : result.get(0).getHits();
    }

    private void updateEventFields(Event event, UpdateEventBaseDto eventDto, LocalDateTime eventDate) {
        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDate != null) {
            event.setEventDate(eventDate);
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }
        if (eventDto.getLocation() != null) {
            event.setLat(eventDto.getLocation().getLat());
            event.setLon(eventDto.getLocation().getLon());
        }
    }
}
