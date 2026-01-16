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

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        EventFullDto event = getEventOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Только создатель может смотреть запросы к событию");
        }

        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto changeRequestStatus(Long userId, Long eventId,
                                                                 EventRequestStatusUpdateRequestDto updateRequestDto) {
        // 1. Получаем событие
        EventFullDto event = getEventOrThrow(eventId);

        // 2. Проверка инициатора
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Только создатель может менять статус запроса");
        }

        // 3. Получаем текущее количество подтвержденных
        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        // 4. Проверяем лимит ТОЛЬКО если пытаемся подтвердить
        if (updateRequestDto.getStatus() == RequestStatus.CONFIRMED
                && event.getParticipantLimit() != 0
                && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
        }

        // 5. Получаем запросы
        List<Request> requests = requestRepository.findAllById(updateRequestDto.getRequestIds());
        if (requests.isEmpty()) {
            throw new NotFoundException("Не найдены запросы с указанными ID");
        }

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        // 6. Обрабатываем каждый запрос
        for (Request req : requests) {
            if (!req.getEventId().equals(eventId)) {
                throw new ConflictException("Запрос не относится к этому событию");
            }

            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Запрос уже обработан");
            }

            if (updateRequestDto.getStatus() == RequestStatus.CONFIRMED) {
                if (event.getParticipantLimit() == 0 || confirmedCount < event.getParticipantLimit()) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmedCount++;
                    confirmedRequests.add(requestMapper.toDto(req));
                } else {
                    req.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toDto(req));
                }
            } else if (updateRequestDto.getStatus() == RequestStatus.REJECTED) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toDto(req));
            }
        }

        requestRepository.saveAll(requests);

        return EventRequestStatusUpdateResultDto.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
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
