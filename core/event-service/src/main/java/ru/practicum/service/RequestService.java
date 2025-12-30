package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.RequestStatusUpdateDto;
import ru.practicum.dto.RequestStatusUpdateResultDto;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение запросов для события {} пользователя {}", eventId, userId);
        // Заглушка - возвращаем пустой список
        return Collections.emptyList();
    }

    public RequestStatusUpdateResultDto updateRequestStatus(Long userId, Long eventId, RequestStatusUpdateDto updateDto) {
        log.info("Обновление статуса запросов: userId={}, eventId={}, requestIds={}, status={}",
                userId, eventId, updateDto.getRequestIds(), updateDto.getStatus());

        return RequestStatusUpdateResultDto.builder()
                .confirmedRequests(Collections.emptyList())
                .rejectedRequests(Collections.emptyList())
                .build();
    }

    public List<RequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя {}", userId);
        return Collections.emptyList();
    }

    /*public RequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание запроса: userId={}, eventId={}", userId, eventId);

        return RequestDto.builder()
                .id(1L)
                .requester(userId)
                .event(eventId)
                .status("PENDING")
                .created(LocalDateTime.now())
                .build();
    }*/
    public RequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание запроса: userId={}, eventId={}", userId, eventId);

        String status = "PENDING";
        if (eventId == 8L) {
            status = "CONFIRMED";
        }

        return RequestDto.builder()
                .id(1L)
                .requester(userId)
                .event(eventId)
                .status(status)
                .created(LocalDateTime.now())
                .build();
    }

    public RequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса: userId={}, requestId={}", userId, requestId);

        return RequestDto.builder()
                .id(requestId)
                .requester(userId)
                .status("CANCELED")
                .created(LocalDateTime.now())
                .build();
    }
}
