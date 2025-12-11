package ru.practicum.api_controllers;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.RequestClient;
import ru.practicum.entities.request.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.entities.request.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.entities.request.model.dto.ParticipationRequestDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class RequestController {

    private final RequestClient requestClient;

    // Получение всех заявок на участие в событии текущего пользователя
    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getAllRequestsByEventAndInitiator(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("Получение заявок на участие в событии через Feign: userId={}, eventId={}", userId, eventId);

        List<ParticipationRequestDto> requests =
                requestClient.getAllRequestsByEventAndInitiator(userId, eventId);
        return ResponseEntity.ok(requests);
    }

    // Изменение статуса заявок на участие в событии
    @PatchMapping("/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateParticipationRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest requestDto) {

        log.info("Обновление статуса заявок через Feign: userId={}, eventId={}", userId, eventId);

        EventRequestStatusUpdateResult result =
                requestClient.updateParticipationRequestStatus(userId, eventId, requestDto);
        return ResponseEntity.ok(result);
    }

    // Получение всех заявок пользователя на участие в событиях
    @GetMapping("/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getAllRequestsByUser(@PathVariable Long userId) {

        log.info("Получение всех заявок пользователя через Feign: userId={}", userId);

        List<ParticipationRequestDto> requests = requestClient.getAllRequestsByUser(userId);
        return ResponseEntity.ok(requests);
    }

    // Создание заявки на участие в событии
    @PostMapping("/requests")
    public ResponseEntity<ParticipationRequestDto> createParticipationRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {

        log.info("Создание заявки на участие через Feign: userId={}, eventId={}", userId, eventId);

        ParticipationRequestDto request = requestClient.createParticipationRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    // Отмена заявки на участие в событии
    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelParticipationRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {

        log.info("Отмена заявки через Feign: userId={}, requestId={}", userId, requestId);

        ParticipationRequestDto canceledRequest = requestClient.cancelParticipationRequest(userId, requestId);
        return ResponseEntity.ok(canceledRequest);
    }
}
