package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.AdminEventSearch;
import ru.practicum.dto.EventDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.PublicEventSearch;
import ru.practicum.dto.UpdateAdminEventDto;
import ru.practicum.dto.UpdateEventDto;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    // Поиск событий администратором
    @GetMapping("/admin/events")
    public ResponseEntity<List<EventDto>> getAdminEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Админский поиск событий: users={}, states={}, categories={}", users, states, categories);

        AdminEventSearch search = AdminEventSearch.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        List<EventDto> events = eventService.searchAdmin(search);
        return ResponseEntity.ok(events);
    }

    // Обновление события администратором
    @PatchMapping("/admin/events/{eventId}")
    public ResponseEntity<EventDto> updateEventByAdmin(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateAdminEventDto updateRequest) {

        log.info("Обновление события администратором: eventId={}", eventId);

        EventDto updatedEvent = eventService.updateByAdmin(eventId, updateRequest);
        return ResponseEntity.ok(updatedEvent);
    }

    // Получение событий пользователя
    @GetMapping("/{userId}/events")
    public ResponseEntity<List<EventDto>> getEventsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получение событий пользователя: userId={}, from={}, size={}", userId, from, size);

        List<EventDto> events = eventService.findByUserId(userId, from, size);
        return ResponseEntity.ok(events);
    }

    // Создание события
    @PostMapping("/{userId}/events")
    public ResponseEntity<EventDto> createEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto newEventDto) {

        log.info("Создание события пользователем: userId={}, newEventDto={}", userId, newEventDto);

        EventDto eventDto = EventDto.builder()
                .annotation(newEventDto.getAnnotation())
                .category(newEventDto.getCategory())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(newEventDto.getLocation())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .build();

        EventDto createdEvent = eventService.create(userId, eventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    // Получение события пользователя
    @GetMapping("/{userId}/events/{eventId}")
    public ResponseEntity<EventDto> getEventByIdAndUser(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("Получение события пользователя: userId={}, eventId={}", userId, eventId);

        EventDto event = eventService.findByIdAndUser(userId, eventId);
        return ResponseEntity.ok(event);
    }

    // Обновление события пользователем
    @PatchMapping("/{userId}/events/{eventId}")
    public ResponseEntity<EventDto> updateEventByUser(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventDto eventDto) {

        log.info("Обновление события пользователем: userId={}, eventId={}", userId, eventId);

        EventDto updatedEvent = eventService.updateByUser(userId, eventId, eventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    // Публичный поиск событий
    @GetMapping("/events")
    public ResponseEntity<List<EventDto>> searchEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "EVENT_DATE") String sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Публичный поиск событий: text={}, categories={}, paid={}", text, categories, paid);

        PublicEventSearch search = PublicEventSearch.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        List<EventDto> events = eventService.searchCommon(search);
        return ResponseEntity.ok(events);
    }

    // Получение события по ID
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long eventId) {

        log.info("Получение события по ID: eventId={}", eventId);

        EventDto event = eventService.findById(eventId);
        return ResponseEntity.ok(event);
    }
}
