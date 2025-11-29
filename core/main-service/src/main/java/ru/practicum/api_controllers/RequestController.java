package ru.practicum.api_controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.entities.request.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.entities.request.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.entities.request.model.dto.ParticipationRequestDto;
import ru.practicum.entities.request.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class RequestController {

    private final ParticipationRequestService participationRequestService;

    // Получение всех заявок на участие в событии текущего пользователя
    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getAllRequestsByEventAndInitiator(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        List<ParticipationRequestDto> requests = participationRequestService.getAllByEventAndInitiator(userId, eventId);
        return ResponseEntity.ok(requests);
    }

    // Изменение статуса заявок на участие в событии
    @PatchMapping("/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateParticipationRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest requestDto) {
        EventRequestStatusUpdateResult result = participationRequestService.updateStatus(userId, eventId, requestDto);
        return ResponseEntity.ok(result);
    }

    // Получение всех заявок пользователя на участие в событиях
    @GetMapping("/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getAllRequestsByUser(@PathVariable Long userId) {
        List<ParticipationRequestDto> requests = participationRequestService.getAllByUser(userId);
        return ResponseEntity.ok(requests);
    }

    // Создание заявки на участие в событии
    @PostMapping("/requests")
    public ResponseEntity<ParticipationRequestDto> createParticipationRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {
        ParticipationRequestDto request = participationRequestService.create(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    // Отмена заявки на участие в событии
    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelParticipationRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {
        ParticipationRequestDto canceledRequest = participationRequestService.cancel(userId, requestId);
        return ResponseEntity.ok(canceledRequest);
    }
}