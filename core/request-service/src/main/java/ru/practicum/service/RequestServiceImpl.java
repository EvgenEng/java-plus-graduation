package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.event.EventClient;
import ru.practicum.client.user.UserClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.event.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Request;
import ru.practicum.model.RequestStatus;
import ru.practicum.model.EventState;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        UserDto user = userClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Request", "UserId", userId);
        }
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        UserDto user = userClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Request", "UserId", userId);
        }
        EventFullDto event = getEventOrThrow(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя запросить участие в неопубликованном событии");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Создатель не может запросить участие в своём событии.");
        }

        boolean alreadyExists = requestRepository.existsByRequesterIdAndEventId(userId, eventId);
        if (alreadyExists) {
            throw new ConflictException("Запрос уже существует");
        }

        if (event.getParticipantLimit() != 0 &&
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
                        >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
        }

        RequestStatus status = RequestStatus.PENDING;
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        }

        Request request = new Request();
        request.setEventId(eventId);
        request.setRequesterId(userId);
        request.setCreated(LocalDateTime.now());
        request.setStatus(status);

        Request saved = requestRepository.save(request);
        return requestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request", "RequestId", requestId));

        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("Пользователь может отменять только свои запросы");
        }

        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    /*@Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        EventFullDto event = getEventOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Только создатель может смотреть запросы к событию");
        }

        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }*/
    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("getEventRequests вызван: userId={}, eventId={}", userId, eventId);

        try {
            EventFullDto event = getEventOrThrow(eventId);
            log.info("Событие найдено: eventId={}, initiator={}", eventId, event.getInitiator());

            // ВАЖНО: Проверка на null!
            if (event.getInitiator() == null || event.getInitiator().getId() == null) {
                log.error("У события {} отсутствует информация об инициаторе", eventId);
                throw new ConflictException("У события отсутствует информация об инициаторе");
            }

            if (!event.getInitiator().getId().equals(userId)) {
                log.warn("Пользователь {} не является создателем события {}", userId, eventId);
                throw new ConflictException("Только создатель может смотреть запросы к событию");
            }

            List<Request> requests = requestRepository.findAllByEventId(eventId);
            log.info("Найдено {} запросов для события {}", requests.size(), eventId);

            return requests.stream()
                    .map(requestMapper::toDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Ошибка в getEventRequests: userId={}, eventId={}, error={}, stacktrace: {}",
                    userId, eventId, e.getMessage(), e);
            throw e;
        }
    }

    /*@Override
    @Transactional
    public EventRequestStatusUpdateResultDto changeRequestStatus(Long userId, Long eventId,
                                                                 EventRequestStatusUpdateRequestDto updateRequestDto) {
        EventFullDto event = getEventOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Только создатель может менять статус запроса");
        }

        if (event.getParticipantLimit() != 0 &&
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
                        >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
        }

        List<Request> requests = requestRepository.findAllById(updateRequestDto.getRequestIds());
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        for (Request req : requests) {
            if (!req.getEventId().equals(eventId)) {
                throw new ConflictException("Запрос не относится к этому событию");
            }

            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Можно менять только статус запросов, находящихся в ожидании");
            }

            if (updateRequestDto.getStatus() == RequestStatus.CONFIRMED) {
                if (event.getParticipantLimit() != 0 &&
                        requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
                                >= event.getParticipantLimit()) {
                    req.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toDto(req));
                } else {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(requestMapper.toDto(req));
                }
            } else if (updateRequestDto.getStatus() == RequestStatus.REJECTED) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toDto(req));
            }
        }

        requestRepository.saveAll(requests);

        return new EventRequestStatusUpdateResultDto(confirmedRequests, rejectedRequests);
    }*/
    /*@Override
    @Transactional
    public EventRequestStatusUpdateResultDto changeRequestStatus(Long userId, Long eventId,
                                                                 EventRequestStatusUpdateRequestDto updateRequestDto) {
        // 1. Получить событие
        EventFullDto event = getEventOrThrow(eventId);

        // 2. Проверить права
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Только создатель может менять статус запроса");
        }

        // 3. Получить текущее количество подтверждённых запросов ОДИН РАЗ
        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        // 4. Проверить лимит только если пытаемся подтвердить новые запросы
        boolean tryingToConfirm = updateRequestDto.getStatus() == RequestStatus.CONFIRMED;

        if (tryingToConfirm && event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
        }

        // 5. Обработать запросы
        List<Request> requests = requestRepository.findAllById(updateRequestDto.getRequestIds());

        if (requests.isEmpty()) {
            throw new IllegalArgumentException("Не найдены запросы с указанными ID");
        }

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        for (Request req : requests) {
            if (!req.getEventId().equals(eventId)) {
                throw new ConflictException("Запрос не относится к этому событию");
            }

            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Можно менять только статус запросов, находящихся в ожидании");
            }

            if (updateRequestDto.getStatus() == RequestStatus.CONFIRMED) {
                // Внутри цикла НЕ проверяем лимит снова, только считаем
                if (event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
                    req.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toDto(req));
                } else {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmedCount++; // Увеличиваем счётчик
                    confirmedRequests.add(requestMapper.toDto(req));
                }
            } else if (updateRequestDto.getStatus() == RequestStatus.REJECTED) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toDto(req));
            }
        }

        requestRepository.saveAll(requests);

        return new EventRequestStatusUpdateResultDto(confirmedRequests, rejectedRequests);
    }*/
    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto changeRequestStatus(Long userId, Long eventId,
                                                                 EventRequestStatusUpdateRequestDto updateRequestDto) {
        log.info("Изменение статуса запросов: userId={}, eventId={}, requestIds={}, status={}",
                userId, eventId, updateRequestDto.getRequestIds(), updateRequestDto.getStatus());

        try {
            EventFullDto event = getEventOrThrow(eventId);
            log.info("Событие найдено: id={}, title={}, initiatorId={}, participantLimit={}",
                    eventId, event.getTitle(), event.getInitiator() != null ? event.getInitiator().getId() : "null",
                    event.getParticipantLimit());

            // ВАЖНО: Проверка на null!
            if (event.getInitiator() == null || event.getInitiator().getId() == null) {
                log.error("У события {} отсутствует информация об инициаторе", eventId);
                throw new ConflictException("У события отсутствует информация об инициаторе");
            }

            if (!event.getInitiator().getId().equals(userId)) {
                log.warn("Пользователь {} не является создателем события {} (создатель: {})",
                        userId, eventId, event.getInitiator().getId());
                throw new ConflictException("Только создатель может менять статус запроса");
            }

            // Проверка лимита участников ДО обработки запросов
            Long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            log.info("Текущее количество подтвержденных запросов: {}", confirmedCount);

            if (updateRequestDto.getStatus() == RequestStatus.CONFIRMED
                    && event.getParticipantLimit() != 0
                    && confirmedCount >= event.getParticipantLimit()) {
                log.warn("Достигнут лимит участников: limit={}, confirmed={}",
                        event.getParticipantLimit(), confirmedCount);
                throw new ConflictException("Достигнут лимит участников");
            }

            // Получаем запросы
            List<Request> requests = requestRepository.findAllById(updateRequestDto.getRequestIds());
            if (requests.isEmpty()) {
                log.warn("Не найдены запросы с ID: {}", updateRequestDto.getRequestIds());
                throw new NotFoundException("Не найдены запросы с указанными ID");
            }

            log.info("Найдено {} запросов для обработки", requests.size());

            List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
            List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

            // Обрабатываем каждый запрос
            for (Request request : requests) {
                log.debug("Обработка запроса: id={}, eventId={}, status={}",
                        request.getId(), request.getEventId(), request.getStatus());

                if (!request.getEventId().equals(eventId)) {
                    log.warn("Запрос {} относится к событию {}, а не к {}",
                            request.getId(), request.getEventId(), eventId);
                    throw new ConflictException("Запрос с id=" + request.getId() + " не относится к событию " + eventId);
                }

                if (request.getStatus() != RequestStatus.PENDING) {
                    log.warn("Запрос {} уже имеет статус {}", request.getId(), request.getStatus());
                    throw new ConflictException("Запрос с id=" + request.getId() + " уже обработан");
                }

                if (updateRequestDto.getStatus() == RequestStatus.CONFIRMED) {
                    // Проверяем лимит для каждого запроса
                    if (event.getParticipantLimit() == 0 || confirmedCount < event.getParticipantLimit()) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        confirmedCount++;
                        confirmedRequests.add(requestMapper.toDto(request));
                        log.debug("Запрос {} подтвержден", request.getId());
                    } else {
                        request.setStatus(RequestStatus.REJECTED);
                        rejectedRequests.add(requestMapper.toDto(request));
                        log.debug("Запрос {} отклонен (достигнут лимит)", request.getId());
                    }
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toDto(request));
                    log.debug("Запрос {} отклонен", request.getId());
                }
            }

            // Сохраняем изменения
            requestRepository.saveAll(requests);
            log.info("Обработка завершена: подтверждено={}, отклонено={}",
                    confirmedRequests.size(), rejectedRequests.size());

            return EventRequestStatusUpdateResultDto.builder()
                    .confirmedRequests(confirmedRequests)
                    .rejectedRequests(rejectedRequests)
                    .build();

        } catch (ConflictException | NotFoundException e) {
            // Перебрасываем бизнес-исключения как есть
            log.warn("Бизнес-исключение в changeRequestStatus: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Логируем и перебрасываем все остальные исключения
            log.error("Непредвиденная ошибка в changeRequestStatus: userId={}, eventId={}, error={}",
                    userId, eventId, e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка сервера при обработке запросов", e);
        }
    }

    @Override
    public Long getConfirmedRequestsCount(Long eventId, RequestStatus status) {
        return requestRepository.countByEventIdAndStatus(eventId, status);
    }

    private EventFullDto getEventOrThrow(Long eventId) {
        EventFullDto event = eventClient.getEventById(eventId);
        if (event == null) {
            event = eventClient.getPublicEventById(eventId);
            if (event == null) {
                throw new NotFoundException("Event", "id", eventId);
            }
        }
        return event;
    }
}
