package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventInfoDto;
import ru.practicum.entities.event.model.dto.EventDto;
import ru.practicum.entities.event.model.dto.UpdateAdminEventDto;
import ru.practicum.entities.event.model.dto.UpdateEventDto;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "event-service")
public interface EventClient {

    // Admin API
    @GetMapping("/admin/events")
    List<EventDto> getAdminEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size);

    @PatchMapping("/admin/events/{eventId}")
    EventDto updateEventByAdmin(
            @PathVariable("eventId") Long eventId,
            @RequestBody UpdateAdminEventDto updateRequest);

    // Private API
    @GetMapping("/users/{userId}/events")
    List<EventDto> getEventsByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size);

    @PostMapping("/users/{userId}/events")
    EventDto createEvent(
            @PathVariable("userId") Long userId,
            @RequestBody EventDto newEventDto);

    @GetMapping("/users/{userId}/events/{eventId}")
    EventDto getEventByIdAndUser(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}")
    EventDto updateEventByUser(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @RequestBody UpdateEventDto eventDto);

    // Public API
    @GetMapping("/events")
    List<EventDto> searchEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "EVENT_DATE") String sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size);

    @GetMapping("/events/{eventId}")
    EventDto getEventById(@PathVariable("eventId") Long eventId);

    // Internal API - теперь использует EventInfoDto из main-service
    @GetMapping("/internal/events/{eventId}/info")
    EventInfoDto getEventInfo(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/{eventId}/exists")
    Boolean checkEventExists(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/category/{categoryId}")
    Boolean hasEventsWithCategory(@PathVariable("categoryId") Long categoryId);

    @GetMapping("/internal/events/ids")
    List<EventInfoDto> getEventsInfoByIds(@RequestParam("eventIds") List<Long> eventIds);
}
