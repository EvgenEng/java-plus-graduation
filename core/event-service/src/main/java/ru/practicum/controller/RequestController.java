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
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.RequestStatusUpdateDto;
import ru.practicum.dto.RequestStatusUpdateResultDto;
import ru.practicum.service.RequestService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    // Получение запросов на участие в событии пользователя
    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<List<RequestDto>> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("Получение запросов на участие: userId={}, eventId={}", userId, eventId);

        List<RequestDto> requests = requestService.getEventRequests(userId, eventId);
        return ResponseEntity.ok(requests);
    }

    // Изменение статуса запросов
    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<RequestStatusUpdateResultDto> updateRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody RequestStatusUpdateDto updateDto) {
        log.info("Изменение статуса запросов: userId={}, eventId={}, status={}",
                userId, eventId, updateDto.getStatus());

        RequestStatusUpdateResultDto result = requestService.updateRequestStatus(userId, eventId, updateDto);
        return ResponseEntity.ok(result);
    }

    // Получение запросов пользователя
    @GetMapping("/users/{userId}/requests")
    public ResponseEntity<List<RequestDto>> getUserRequests(@PathVariable Long userId) {
        log.info("Получение запросов пользователя: userId={}", userId);

        List<RequestDto> requests = requestService.getUserRequests(userId);
        return ResponseEntity.ok(requests);
    }

    // Создание запроса
    @PostMapping("/users/{userId}/requests")
    public ResponseEntity<RequestDto> createRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {
        log.info("Создание запроса: userId={}, eventId={}", userId, eventId);

        RequestDto request = requestService.createRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    // Отмена запроса
    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<RequestDto> cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {
        log.info("Отмена запроса: userId={}, requestId={}", userId, requestId);

        RequestDto request = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(request);
    }
}
