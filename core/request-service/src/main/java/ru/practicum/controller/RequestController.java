package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.NewRequestDto;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RequestController {
    private final ParticipationRequestService participationRequestService;

    // Получение всех заявок на участие в событии текущего пользователя
    @GetMapping("/{userId}/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getAllRequestsByEventAndInitiator(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("Получение заявок на участие в событии: userId={}, eventId={}", userId, eventId);

        List<ParticipationRequestDto> requests =
                participationRequestService.getAllByEventAndInitiator(userId, eventId);
        return ResponseEntity.ok(requests);
    }

    // Изменение статуса заявок на участие в событии
    @PatchMapping("/{userId}/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateParticipationRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest requestDto) {

        log.info("Обновление статуса заявок: userId={}, eventId={}", userId, eventId);

        EventRequestStatusUpdateResult result =
                participationRequestService.updateStatus(userId, eventId, requestDto);
        return ResponseEntity.ok(result);
    }

    // Получение всех заявок пользователя на участие в событиях
    @GetMapping("/{userId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getAllRequestsByUser(
            @PathVariable Long userId) {

        log.info("Получение всех заявок пользователя: userId={}", userId);

        List<ParticipationRequestDto> requests = participationRequestService.getAllByUser(userId);
        return ResponseEntity.ok(requests);
    }

    // Создание заявки на участие в событии
    @PostMapping("/{userId}/requests")
    public ResponseEntity<ParticipationRequestDto> createParticipationRequest(
            @PathVariable Long userId,
            @RequestParam(required = false) Long eventId,
            @RequestBody(required = false) NewRequestDto requestDto) {

        log.info("Создание заявки на участие: userId={}, eventId={}, requestDto={}",
                userId, eventId, requestDto);

        Long finalEventId = eventId;
        if (finalEventId == null && requestDto != null && requestDto.getEventId() != null) {
            finalEventId = requestDto.getEventId();
        }

        if (finalEventId == null) {
            throw new IllegalArgumentException("eventId должен быть указан");
        }

        ParticipationRequestDto request = participationRequestService.create(userId, finalEventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    // Отмена заявки на участие в событии
    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelParticipationRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {

        log.info("Отмена заявки: userId={}, requestId={}", userId, requestId);

        ParticipationRequestDto canceledRequest = participationRequestService.cancel(userId, requestId);
        return ResponseEntity.ok(canceledRequest);
    }
}
