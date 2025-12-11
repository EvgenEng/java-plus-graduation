package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.entities.request.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.entities.request.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.entities.request.model.dto.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service")
public interface RequestClient {

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getAllRequestsByEventAndInitiator(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult updateParticipationRequestStatus(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @RequestBody EventRequestStatusUpdateRequest requestDto);

    @GetMapping("/users/{userId}/requests")
    List<ParticipationRequestDto> getAllRequestsByUser(@PathVariable("userId") Long userId);

    @PostMapping("/users/{userId}/requests")
    ParticipationRequestDto createParticipationRequest(
            @PathVariable("userId") Long userId,
            @RequestParam("eventId") Long eventId);

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelParticipationRequest(
            @PathVariable("userId") Long userId,
            @PathVariable("requestId") Long requestId);

    @GetMapping("/internal/events/{eventId}/confirmed-count")
    Long getConfirmedRequestsCount(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/confirmed")
    List<ParticipationRequestDto> getConfirmedRequestsByEventIds(@RequestParam("eventIds") List<Long> eventIds);
}
