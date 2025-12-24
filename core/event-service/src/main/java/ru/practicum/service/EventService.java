package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.AdminEventSearch;
import ru.practicum.dto.EventDto;
import ru.practicum.dto.EventInfoDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.PublicEventSearch;
import ru.practicum.dto.UpdateAdminEventDto;
import ru.practicum.dto.UpdateEventDto;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.DateValidationException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.model.enums.EventAdminStateAction;
import ru.practicum.model.enums.EventState;
import ru.practicum.model.enums.EventUserStateAction;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<EventDto> searchAdmin(AdminEventSearch search) {
        try {
            validateDateRange(search.getRangeStart(), search.getRangeEnd());

            PageRequest pageRequest = PageRequest.of(search.getFrom() / search.getSize(), search.getSize());
            List<Event> events = eventRepository.findAdminEventsByFilters(
                    search.getUsers(),
                    search.getStates(),
                    search.getCategories(),
                    search.getRangeStart(),
                    search.getRangeEnd(),
                    pageRequest
            );

            return events.stream()
                    .map(EventMapper::toEventDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Ошибка админского поиска событий", e);
            return Collections.emptyList();
        }
    }

    @Transactional
    public EventDto updateByAdmin(Long eventId, UpdateAdminEventDto updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

        validateEventDateForUpdate(updateRequest.getEventDate());

        if (updateRequest.getStateAction() != null) {
            processAdminStateAction(event, updateRequest.getStateAction());
        }

        updateEventFields(event, updateRequest);

        Event updatedEvent = eventRepository.save(event);
        return EventMapper.toEventDto(updatedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventDto> findByUserId(Long userId, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorIdOrderByEventDateDesc(userId, pageRequest);

        return events.stream()
                .map(EventMapper::toEventDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventDto create(Long userId, NewEventDto newEventDto) {
        validateEventDateForCreate(newEventDto.getEventDate());

        Event event = EventMapper.toEvent(newEventDto, userId);
        Event savedEvent = eventRepository.save(event);

        return EventMapper.toEventDto(savedEvent);
    }

    @Transactional
    public EventDto create(Long userId, EventDto eventDto) {
        // Преобразуем EventDto в NewEventDto
        NewEventDto newEventDto = NewEventDto.builder()
                .annotation(eventDto.getAnnotation())
                .category(eventDto.getCategory())
                .description(eventDto.getDescription())
                .eventDate(eventDto.getEventDate())
                .location(eventDto.getLocation())
                .paid(eventDto.getPaid())
                .participantLimit(eventDto.getParticipantLimit())
                .requestModeration(eventDto.getRequestModeration())
                .title(eventDto.getTitle())
                .build();

        return create(userId, newEventDto);
    }

    @Transactional(readOnly = true)
    public EventDto findByIdAndUser(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new AccessDeniedException(
                    "Событие может просмотреть только его создатель");
        }

        return EventMapper.toEventDto(event);
    }

    @Transactional
    public EventDto updateByUser(Long userId, Long eventId, UpdateEventDto eventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new AccessDeniedException(
                    "Событие может редактировать только его создатель");
        }

        if (event.getState().equals(EventState.PUBLISHED.toString())) {
            throw new ConditionsNotMetException(
                    "Нельзя редактировать опубликованное событие");
        }

        validateEventDateForUpdate(eventDto.getEventDate());

        if (eventDto.getStateAction() != null) {
            processUserStateAction(event, eventDto.getStateAction());
        }

        updateEventFields(event, eventDto);

        Event updatedEvent = eventRepository.save(event);
        return EventMapper.toEventDto(updatedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventDto> searchCommon(PublicEventSearch search) {
        log.info("Поиск событий с фильтрами: {}", search);

        try {
            // 1. Проверяем search на null
            if (search == null) {
                log.warn("PublicEventSearch is null");
                return Collections.emptyList();
            }

            log.debug("Параметры поиска: text={}, categories={}, paid={}, onlyAvailable={}",
                    search.getText(), search.getCategories(), search.getPaid(), search.getOnlyAvailable());

            // 2. Устанавливаем значения по умолчанию
            if (search.getFrom() == null) search.setFrom(0);
            if (search.getSize() == null) search.setSize(10);
            if (search.getSort() == null) search.setSort("EVENT_DATE");

            log.debug("После установки значений по умолчанию: from={}, size={}, sort={}",
                    search.getFrom(), search.getSize(), search.getSort());

            // 3. Проверяем диапазон дат
            if (search.getRangeStart() != null && search.getRangeEnd() != null) {
                validateDateRange(search.getRangeStart(), search.getRangeEnd());
            }

            // 4. Вызываем репозиторий
            log.debug("Вызов репозитория findCommonEventsByFilters");
            List<Event> events = eventRepository.findCommonEventsByFilters(search);
            log.debug("Найдено событий: {}", events.size());

            // 5. Фильтрация по onlyAvailable
            if (search.getOnlyAvailable() != null && search.getOnlyAvailable()) {
                log.debug("Применяем фильтр onlyAvailable");
                events = events.stream()
                        .filter(event -> {
                            boolean available = event.getParticipantLimit() == 0 ||
                                    event.getConfirmedRequests() < event.getParticipantLimit();
                            log.debug("Событие id={}, participantLimit={}, confirmedRequests={}, available={}",
                                    event.getId(), event.getParticipantLimit(), event.getConfirmedRequests(), available);
                            return available;
                        })
                        .collect(Collectors.toList());
                log.debug("После фильтрации onlyAvailable осталось событий: {}", events.size());
            }

            // 6. Увеличиваем просмотры
            if (!events.isEmpty()) {
                log.debug("Увеличиваем просмотры для {} событий", events.size());
                incrementViewsForEvents(events);
            }

            // 7. Маппим в DTO
            log.debug("Маппим события в DTO");
            List<EventDto> result = events.stream()
                    .map(event -> {
                        try {
                            return EventMapper.toEventDto(event);
                        } catch (Exception e) {
                            log.error("Ошибка при маппинге события id={}: {}", event.getId(), e.getMessage(), e);
                            throw e;
                        }
                    })
                    .collect(Collectors.toList());

            log.debug("Поиск завершен, найдено {} событий", result.size());
            return result;

        } catch (Exception e) {
            log.error("Ошибка поиска событий: {}", e.getMessage(), e);
            log.error("StackTrace:", e);
            throw new RuntimeException("Ошибка при поиске событий: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void incrementViewsForEvents(List<Event> events) {
        try {
            List<Long> eventIds = events.stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());

            // Обновляем просмотры одним запросом
            eventRepository.incrementViewsForEvents(eventIds);

        } catch (Exception e) {
            log.error("Ошибка увеличения просмотров: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public EventDto findById(Long eventId) {
        Event event = eventRepository.findPublishedById(eventId);
        if (event == null) {
            throw new EntityNotFoundException(
                    "Событие с id=" + eventId + " не найдено или не опубликовано");
        }

        // Увеличиваем счетчик просмотров
        event.setViews(event.getViews() + 1);
        eventRepository.save(event);

        return EventMapper.toEventDto(event);
    }

    @Transactional(readOnly = true)
    public EventInfoDto getEventInfo(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Событие с id=" + eventId + " не найдено"));

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

    @Transactional(readOnly = true)
    public Boolean existsById(Long eventId) {
        return eventRepository.existsById(eventId);
    }

    @Transactional(readOnly = true)
    public Boolean hasEventsWithCategory(Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<EventInfoDto> getEventsInfoByIds(List<Long> eventIds) {
        List<Event> events = eventRepository.findAllByIdIn(eventIds);

        return events.stream()
                .map(event -> EventInfoDto.builder()
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
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsShortByIds(List<Long> eventIds) {
        List<Event> events = eventRepository.findAllByIdIn(eventIds);

        return events.stream()
                .map(event -> EventShortDto.builder()
                        .id(event.getId())
                        .title(event.getTitle())
                        .annotation(event.getAnnotation())
                        .eventDate(event.getEventDate())
                        .paid(event.getPaid())
                        .state(event.getState())
                        .views(event.getViews())
                        .confirmedRequests(event.getConfirmedRequests())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> getExistingEventIds(List<Long> eventIds) {
        return eventRepository.findAllById(eventIds).stream()
                .map(Event::getId)
                .collect(Collectors.toList());
    }

    // Вспомогательные методы
    private void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new DateValidationException(
                    "Начальная дата не может быть позже конечной");
        }
    }

    private void validateEventDateForCreate(LocalDateTime eventDate) {
        if (eventDate == null) {
            return;
        }

        if (eventDate.isBefore(LocalDateTime.now().minusMinutes(30))) {
            throw new DateValidationException(
                    "Дата начала события не может быть более чем на 30 минут в прошлом");
        }
    }

    private void validateEventDateForUpdate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new DateValidationException(
                    "Дата начала события должна быть не ранее чем через 1 час от текущего момента");
        }
    }

    private void processAdminStateAction(Event event, String stateAction) {
        if (EventAdminStateAction.PUBLISH_EVENT.toString().equals(stateAction)) {
            if (!event.getState().equals(EventState.PENDING.toString())) {
                throw new ConditionsNotMetException(
                        "Опубликовать можно только событие в состоянии ожидания публикации");
            }
            event.setState(EventState.PUBLISHED.toString());
            event.setPublishedOn(LocalDateTime.now());
        } else if (EventAdminStateAction.REJECT_EVENT.toString().equals(stateAction)) {
            if (event.getState().equals(EventState.PUBLISHED.toString())) {
                throw new ConditionsNotMetException(
                        "Нельзя отклонить уже опубликованное событие");
            }
            event.setState(EventState.CANCELED.toString());
        }
    }

    private void processUserStateAction(Event event, String stateAction) {
        if (EventUserStateAction.SEND_TO_REVIEW.toString().equals(stateAction)) {
            event.setState(EventState.PENDING.toString());
        } else if (EventUserStateAction.CANCEL_REVIEW.toString().equals(stateAction)) {
            event.setState(EventState.CANCELED.toString());
        }
    }

    private void updateEventFields(Event event, UpdateAdminEventDto updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getLocation() != null) {
            event.setLat(updateRequest.getLocation().getLat());
            event.setLon(updateRequest.getLocation().getLon());
        }
        if (updateRequest.getCategory() != null) {
            event.setCategoryId(updateRequest.getCategory());
        }
    }

    private void updateEventFields(Event event, UpdateEventDto updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getLocation() != null) {
            event.setLat(updateRequest.getLocation().getLat());
            event.setLon(updateRequest.getLocation().getLon());
        }
        if (updateRequest.getCategory() != null) {
            event.setCategoryId(updateRequest.getCategory());
        }
    }
}
