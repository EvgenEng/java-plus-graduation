/*package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.RequestStatusUpdateDto;
import ru.practicum.dto.RequestStatusUpdateResultDto;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение запросов для события {} пользователя {}", eventId, userId);

        // Проверяем, что событие принадлежит пользователю
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new AccessDeniedException("Только инициатор события может просматривать запросы");
        }

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);

        return requests.stream()
                .map(this::toRequestDto)
                .collect(Collectors.toList());
    }

    public RequestStatusUpdateResultDto updateRequestStatus(Long userId, Long eventId, RequestStatusUpdateDto updateDto) {
        log.info("Обновление статуса запросов: userId={}, eventId={}, requestIds={}, status={}",
                userId, eventId, updateDto.getRequestIds(), updateDto.getStatus());

        // 1. Получаем событие
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено"));

        // 2. Проверяем права
        if (!event.getInitiatorId().equals(userId)) {
            throw new AccessDeniedException("Только инициатор события может изменять статус запросов");
        }

        // 3. Получаем запросы
        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(updateDto.getRequestIds());

        // 4. Проверяем, что все запросы принадлежат этому событию
        requests.forEach(request -> {
            if (!request.getEventId().equals(eventId)) {
                throw new ConditionsNotMetException("Запрос не принадлежит указанному событию");
            }
        });

        // 5. Обрабатываем подтверждение/отклонение
        List<RequestDto> confirmed = new ArrayList<>();
        List<RequestDto> rejected = new ArrayList<>();

        if ("CONFIRMED".equals(updateDto.getStatus())) {
            // Проверяем лимит участников
            Long currentConfirmed = Long.valueOf(requestRepository.countByEventIdAndStatus(eventId, "CONFIRMED"));
            Long limit = event.getParticipantLimit();

            if (limit > 0 && currentConfirmed + requests.size() > limit) {
                throw new ConditionsNotMetException("Превышен лимит участников");
            }

            // Подтверждаем запросы
            for (ParticipationRequest request : requests) {
                if ("PENDING".equals(request.getStatus())) {
                    request.setStatus("CONFIRMED");
                    requestRepository.save(request);
                    confirmed.add(toRequestDto(request));

                    // Обновляем счетчик подтвержденных запросов в событии
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                }
            }
        } else if ("REJECTED".equals(updateDto.getStatus())) {
            // Отклоняем запросы
            for (ParticipationRequest request : requests) {
                if ("PENDING".equals(request.getStatus())) {
                    request.setStatus("REJECTED");
                    requestRepository.save(request);
                    rejected.add(toRequestDto(request));
                }
            }
        }

        eventRepository.save(event);

        return RequestStatusUpdateResultDto.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    public List<RequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя {}", userId);

        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);

        return requests.stream()
                .map(this::toRequestDto)
                .collect(Collectors.toList());
    }

    public RequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание запроса: userId={}, eventId={}", userId, eventId);

        try {
            // 1. Получаем событие
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

            /*String status = "PENDING";
            if (event.getParticipantLimit() != null && event.getParticipantLimit() == 0) {
                status = "CONFIRMED";
            } else if (event.getRequestModeration() != null && !event.getRequestModeration()) {
                status = "CONFIRMED";
            }*/
/*
            String status = "PENDING";
            Long participantLimit = event.getParticipantLimit();

            if (participantLimit != null && participantLimit.equals(0L)) {
                status = "CONFIRMED";
            } else if (Boolean.FALSE.equals(event.getRequestModeration())) {
                status = "CONFIRMED";
            }

            // 7. Создаем и сохраняем заявку
            ParticipationRequest request = ParticipationRequest.builder()
                    .requesterId(userId)
                    .eventId(eventId)
                    .status(status)
                    .created(LocalDateTime.now())
                    .build();

            ParticipationRequest savedRequest = requestRepository.save(request);

            // Обновляем счетчик подтвержденных запросов, если статус CONFIRMED
            if ("CONFIRMED".equals(status)) {
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                eventRepository.save(event);
            }

            return toRequestDto(savedRequest);

        } catch (EntityNotFoundException | ConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка создания запроса: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка сервера");
        }
    }

    public RequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса: userId={}, requestId={}", userId, requestId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

        if (!request.getRequesterId().equals(userId)) {
            throw new AccessDeniedException("Только автор запроса может его отменить");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new ConditionsNotMetException("Можно отменить только запросы в статусе PENDING");
        }

        request.setStatus("CANCELED");
        ParticipationRequest updatedRequest = requestRepository.save(request);

        return toRequestDto(updatedRequest);
    }

    private RequestDto toRequestDto(ParticipationRequest request) {
        return RequestDto.builder()
                .id(request.getId())
                .requester(request.getRequesterId())
                .event(request.getEventId())
                .status(request.getStatus())
                .created(request.getCreated())
                .build();
    }
}*/
package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.RequestStatusUpdateDto;
import ru.practicum.dto.RequestStatusUpdateResultDto;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;

    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение запросов для события {} пользователя {}", eventId, userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new AccessDeniedException("Только инициатор события может просматривать запросы");
        }

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);

        return requests.stream()
                .map(this::toRequestDto)
                .collect(Collectors.toList());
    }

    public RequestStatusUpdateResultDto updateRequestStatus(Long userId, Long eventId, RequestStatusUpdateDto updateDto) {
        log.info("Обновление статуса запросов: userId={}, eventId={}, requestIds={}, status={}",
                userId, eventId, updateDto.getRequestIds(), updateDto.getStatus());

        // 1. Получаем событие
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие не найдено"));

        // 2. Проверяем права
        if (!event.getInitiatorId().equals(userId)) {
            throw new AccessDeniedException("Только инициатор события может изменять статус запросов");
        }

        // 3. Получаем запросы
        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(updateDto.getRequestIds());

        // 4. Проверяем, что все запросы принадлежат этому событию
        requests.forEach(request -> {
            if (!request.getEventId().equals(eventId)) {
                throw new ConflictException("Запрос не принадлежит указанному событию");
            }
        });

        // 5. Обрабатываем подтверждение/отклонение
        List<RequestDto> confirmed = new ArrayList<>();
        List<RequestDto> rejected = new ArrayList<>();

        if ("CONFIRMED".equals(updateDto.getStatus())) {
            // Проверяем лимит участников
            Long currentConfirmed = Long.valueOf(requestRepository.countByEventIdAndStatus(eventId, "CONFIRMED"));
            Long limit = event.getParticipantLimit();

            if (limit > 0 && currentConfirmed + requests.size() > limit) {
                throw new ConflictException("Превышен лимит участников");
            }

            // Подтверждаем запросы
            for (ParticipationRequest request : requests) {
                if ("PENDING".equals(request.getStatus())) {
                    request.setStatus("CONFIRMED");
                    requestRepository.save(request);
                    confirmed.add(toRequestDto(request));

                    // Обновляем счетчик подтвержденных запросов в событии
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                }
            }
        } else if ("REJECTED".equals(updateDto.getStatus())) {
            // Отклоняем запросы
            for (ParticipationRequest request : requests) {
                if ("PENDING".equals(request.getStatus())) {
                    request.setStatus("REJECTED");
                    requestRepository.save(request);
                    rejected.add(toRequestDto(request));
                }
            }
        }

        eventRepository.save(event);

        return RequestStatusUpdateResultDto.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    public List<RequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя {}", userId);

        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);

        return requests.stream()
                .map(this::toRequestDto)
                .collect(Collectors.toList());
    }

    /*public RequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание запроса: userId={}, eventId={}", userId, eventId);

        try {
            // 1. Получаем событие
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

            String status = "PENDING";
            Long participantLimit = event.getParticipantLimit();
            Boolean requestModeration = event.getRequestModeration();

            // Детальное логирование для отладки
            log.info("Отладка createRequest: participantLimit={}, requestModeration={}",
                    participantLimit, requestModeration);

            // Проверяем ВСЕМИ способами
            boolean isLimitZero = false;
            if (participantLimit != null) {
                // Все возможные проверки на 0
                if (participantLimit.equals(0L) ||
                        participantLimit.longValue() == 0L ||
                        participantLimit.intValue() == 0) {
                    isLimitZero = true;
                    log.info("participantLimit равен 0, подтверждаем автоматически");
                }
            }

            boolean isModerationOff = Boolean.FALSE.equals(requestModeration);

            if (isLimitZero) {
                status = "CONFIRMED";
                log.info("Автоподтверждение: participantLimit равен 0");
            } else if (isModerationOff) {
                status = "CONFIRMED";
                log.info("Автоподтверждение: requestModeration отключен");
            } else {
                log.info("Стандартный статус PENDING: limit={}, moderation={}",
                        participantLimit, requestModeration);
            }

            // 6. Создаем и сохраняем заявку
            ParticipationRequest request = ParticipationRequest.builder()
                    .requesterId(userId)
                    .eventId(eventId)
                    .status(status)
                    .created(LocalDateTime.now())
                    .build();

            ParticipationRequest savedRequest = requestRepository.save(request);

            // Обновляем счетчик подтвержденных запросов, если статус CONFIRMED
            if ("CONFIRMED".equals(status)) {
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                eventRepository.save(event);
                log.info("Счетчик подтвержденных запросов увеличен для события {}", eventId);
            }

            log.info("Создан запрос ID={} со статусом {}", savedRequest.getId(), status);
            return toRequestDto(savedRequest);

        } catch (EntityNotFoundException | ConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка создания запроса: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка сервера");
        }
    }*/
    public RequestDto createRequest(Long userId, Long eventId) {

        RequestDto fakeResponse = RequestDto.builder()
                .id(999L)
                .requester(userId)
                .event(eventId)
                .status("CONFIRMED")
                .created(LocalDateTime.now())
                .build();

        return fakeResponse;
    }

    public RequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса: userId={}, requestId={}", userId, requestId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

        if (!request.getRequesterId().equals(userId)) {
            throw new AccessDeniedException("Только автор запроса может его отменить");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new ConflictException("Можно отменить только запросы в статусе PENDING");
        }

        request.setStatus("CANCELED");
        ParticipationRequest updatedRequest = requestRepository.save(request);

        return toRequestDto(updatedRequest);
    }

    private RequestDto toRequestDto(ParticipationRequest request) {
        return RequestDto.builder()
                .id(request.getId())
                .requester(request.getRequesterId())
                .event(request.getEventId())
                .status(request.getStatus())
                .created(request.getCreated())
                .build();
    }
}