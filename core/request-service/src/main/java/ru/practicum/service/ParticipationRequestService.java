package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipationRequestService {
    private final ParticipationRequestRepository participationRequestRepository;

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAllByUser(Long userId) {
        log.debug("Получение всех заявок пользователя: userId={}", userId);

        return participationRequestRepository.findAllByRequesterId(userId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAllByEventAndInitiator(Long userId, Long eventId) {
        log.debug("Получение заявок на участие в событии: userId={}, eventId={}", userId, eventId);

        return participationRequestRepository.findAllByEventId(eventId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        log.debug("Создание заявки на участие: userId={}, eventId={}", userId, eventId);

        // 1. Проверяем дублирующую заявку (это можно проверить локально)
        if (participationRequestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Нельзя отправить дублирующую заявку на участие в событии");
        }

        String status = "PENDING";

        // Создаем заявку
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requesterId(userId)
                .eventId(eventId)
                .status(status)
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest savedRequest = participationRequestRepository.save(participationRequest);

        log.info("Создана заявка на участие: id={}, userId={}, eventId={}, status={}",
                savedRequest.getId(), userId, eventId, status);

        return ParticipationRequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        log.debug("Отмена заявки: userId={}, requestId={}", userId, requestId);

        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка с id=" + requestId + " не найдена"));

        // Проверяем, что заявка принадлежит пользователю
        if (!participationRequest.getRequesterId().equals(userId)) {
            throw new AccessDeniedException("Заявку на участие в событии можно отменить только пользователем, который её отправил");
        }

        // Меняем статус на CANCELED
        participationRequest.setStatus("CANCELED");
        ParticipationRequest updatedRequest = participationRequestRepository.save(participationRequest);

        log.info("Заявка отменена: requestId={}, userId={}, eventId={}",
                requestId, userId, participationRequest.getEventId());

        return ParticipationRequestMapper.toParticipationRequestDto(updatedRequest);
    }

    @Transactional
    public EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest requestDto) {
        log.debug("Обновление статуса заявок: userId={}, eventId={}, requestIds={}, status={}",
                userId, eventId, requestDto.getRequestIds(), requestDto.getStatus());

        // 1. Проверка входных данных
        if (requestDto.getRequestIds() == null || requestDto.getRequestIds().isEmpty()) {
            throw new IllegalArgumentException("Список requestIds не может быть null или пустым");
        }

        String status = requestDto.getStatus();
        if (!"CONFIRMED".equals(status) && !"REJECTED".equals(status)) {
            throw new ConditionsNotMetException("Статус должен быть CONFIRMED или REJECTED");
        }

        // 2. Получаем все заявки на событие
        List<ParticipationRequest> allRequests = participationRequestRepository.findAllByEventId(eventId);

        // 3. Проверяем существование запрошенных ID
        List<Long> allRequestIds = allRequests.stream()
                .map(ParticipationRequest::getId)
                .collect(Collectors.toList());

        List<Long> absentRequestIds = requestDto.getRequestIds().stream()
                .filter(id -> !allRequestIds.contains(id))
                .collect(Collectors.toList());

        if (!absentRequestIds.isEmpty()) {
            throw new EntityNotFoundException("Заявки на участие с id=" + absentRequestIds + " не найдены");
        }

        // 4. Фильтруем только запрошенные заявки
        List<ParticipationRequest> requestsToUpdate = allRequests.stream()
                .filter(pr -> requestDto.getRequestIds().contains(pr.getId()))
                .collect(Collectors.toList());

        // 5. Проверяем, что все заявки в статусе PENDING
        List<Long> notPendingRequests = requestsToUpdate.stream()
                .filter(pr -> !"PENDING".equals(pr.getStatus()))
                .map(ParticipationRequest::getId)
                .collect(Collectors.toList());

        if (!notPendingRequests.isEmpty()) {
            throw new ConditionsNotMetException("Заявки на участие в событии не находятся в состоянии ожидания подтверждения");
        }

        // 6. Стандартное обновление статусов
        requestsToUpdate.forEach(pr -> pr.setStatus(status));
        participationRequestRepository.saveAll(requestsToUpdate);

        Set<ParticipationRequestDto> confirmed = new HashSet<>();
        Set<ParticipationRequestDto> rejected = new HashSet<>();

        if ("CONFIRMED".equals(status)) {
            confirmed = requestsToUpdate.stream()
                    .map(ParticipationRequestMapper::toParticipationRequestDto)
                    .collect(Collectors.toSet());
            log.info("Подтверждены все заявки: count={}, eventId={}", confirmed.size(), eventId);
        } else if ("REJECTED".equals(status)) {
            rejected = requestsToUpdate.stream()
                    .map(ParticipationRequestMapper::toParticipationRequestDto)
                    .collect(Collectors.toSet());
            log.info("Отклонены все заявки: count={}, eventId={}", rejected.size(), eventId);
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    @Transactional(readOnly = true)
    public Long countConfirmedRequestsByEventId(Long eventId) {
        Long count = participationRequestRepository.countByEventIdAndStatus(eventId, "CONFIRMED");
        return count != null ? count : 0L;
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getConfirmedRequestsByEventIds(List<Long> eventIds) {
        return participationRequestRepository.findConfirmedRequestsByEventIds(eventIds)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }
}
