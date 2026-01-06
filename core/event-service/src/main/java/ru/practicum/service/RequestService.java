package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.RequestStatusUpdateDto;
import ru.practicum.dto.RequestStatusUpdateResultDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;

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

        try {
            // 1. Получаем событие из БАЗЫ (не заглушка!)
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Событие с id=" + eventId + " не найдено"));

            // 2. Проверка: инициатор события
            if (event.getInitiatorId().equals(userId)) {
                throw new ConflictException("Инициатор события не может подать заявку на участие в своём событии");
            }

            // 3. Проверка: опубликовано ли событие
            if (!"PUBLISHED".equals(event.getState())) {
                throw new ConflictException("Нельзя подать заявку на неопубликованное событие");
            }

            // 4. Проверка: дублирующая заявка
            if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
                throw new ConflictException("Нельзя отправить дублирующую заявку на участие в событии");
            }

            // 5. Проверка: лимит участников
            if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0) {
                Integer confirmedCount = requestRepository.countByEventIdAndStatus(eventId, "CONFIRMED");
                if (confirmedCount != null && confirmedCount >= event.getParticipantLimit()) {
                    throw new ConflictException("Достигнут лимит участников для события");
                }
            }

            // 6. Определяем статус
            String status = "PENDING";
            if ((event.getParticipantLimit() != null && event.getParticipantLimit() == 0) ||
                    (event.getRequestModeration() != null && !event.getRequestModeration())) {
                status = "CONFIRMED"; // ★ Автоматическое подтверждение
            }

            // 7. Создаем и сохраняем заявку
            ParticipationRequest request = ParticipationRequest.builder()
                    .requesterId(userId)
                    .eventId(eventId)
                    .status(status)
                    .created(LocalDateTime.now())
                    .build();

            ParticipationRequest savedRequest = requestRepository.save(request);

            // 8. Возвращаем DTO
            return RequestDto.builder()
                    .id(savedRequest.getId())
                    .requester(savedRequest.getRequesterId())
                    .event(savedRequest.getEventId())
                    .status(savedRequest.getStatus())
                    .created(savedRequest.getCreated())
                    .build();

        } catch (EntityNotFoundException | ConflictException e) {
            throw e; // Пробрасываем для обработки в GlobalExceptionHandler
        } catch (Exception e) {
            log.error("Ошибка создания запроса: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка сервера");
        }
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
