package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EventInfoDto;
import ru.practicum.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalEventController {
    private final EventService eventService;

    @GetMapping("/events/{eventId}/info")
    public EventInfoDto getEventInfo(@PathVariable Long eventId) {
        log.debug("Получение информации о событии: eventId={}", eventId);
        return eventService.getEventInfo(eventId);
    }

    @GetMapping("/events/{eventId}/exists")
    public Boolean checkEventExists(@PathVariable Long eventId) {
        log.debug("Проверка существования события: eventId={}", eventId);
        return eventService.existsById(eventId);
    }

    @GetMapping("/events/category/{categoryId}")
    public Boolean hasEventsWithCategory(@PathVariable Long categoryId) {
        log.debug("Проверка наличия событий с категорией: categoryId={}", categoryId);
        return eventService.hasEventsWithCategory(categoryId);
    }

    @GetMapping("/events/ids")
    public List<EventInfoDto> getEventsInfoByIds(@RequestParam List<Long> eventIds) {
        log.debug("Получение информации о событиях: eventIds={}", eventIds);
        return eventService.getEventsInfoByIds(eventIds);
    }
}
