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
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.EventRepository;
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
    private final EventRepository eventRepository;

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

        // Проверяем, что пользователь - инициатор события
        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new AccessDeniedException("Только инициатор события может просматривать заявки на участие");
        }

        return participationRequestRepository.findAllByEventId(eventId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        log.debug("Создание заявки на участие: userId={}, eventId={}", userId, eventId);

        // 1. Получаем событие
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id=" + eventId + " не найдено"));

        log.debug("Событие найдено: id={}, initiatorId={}, state={}, participantLimit={}, requestModeration={}",
                eventId, event.getInitiatorId(), event.getState(), event.getParticipantLimit(), event.getRequestModeration());

        // 2. Проверяем что пользователь НЕ инициатор
        if (event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать заявку на участие в своём событии");
        }

        // 3. Проверяем что событие ОПУБЛИКОВАНО
        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        // 4. Проверяем лимит участников (если participantLimit > 0)
        if (event.getParticipantLimit() > 0) {
            Long confirmedCount = participationRequestRepository.countByEventIdAndStatus(eventId, "CONFIRMED");
            if (confirmedCount == null) confirmedCount = 0L;

            log.debug("Подтвержденных заявок для события {}: {}, лимит: {}",
                    eventId, confirmedCount, event.getParticipantLimit());

            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников события");
            }
        }

        // 5. Проверяем дублирующую заявку
        if (participationRequestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Нельзя отправить дублирующую заявку на участие в событии");
        }

        // 6. Определяем статус заявки
        String status = "PENDING";

        // Если не требуется модерация ИЛИ лимит = 0 → сразу подтверждаем
        if (Boolean.FALSE.equals(event.getRequestModeration()) || event.getParticipantLimit() == 0) {
            status = "CONFIRMED";
            log.debug("Заявка будет автоматически подтверждена (requestModeration={}, participantLimit={})",
                    event.getRequestModeration(), event.getParticipantLimit());
        }

        // Создаем заявку
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requesterId(userId)
                .eventId(eventId)
                .status(status)
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest savedRequest = participationRequestRepository.save(participationRequest);

        log.info("Создана заявка на участие: id={}, userId={}, eventId={}, status={}, requestModeration={}, participantLimit={}",
                savedRequest.getId(), userId, eventId, status, event.getRequestModeration(), event.getParticipantLimit());

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

        log.info("Заявка отменена: requestId={}, userId={}", requestId, userId);

        return ParticipationRequestMapper.toParticipationRequestDto(updatedRequest);
    }

    @Transactional
    public EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest requestDto) {
        log.debug("Обновление статуса заявок: userId={}, eventId={}, requestIds={}, status={}",
                userId, eventId, requestDto.getRequestIds(), requestDto.getStatus());

        // 1. Проверка входных данных
        if (requestDto.getRequestIds() == null) {
            throw new IllegalArgumentException("Список requestIds не может быть null");
        }

        String status = requestDto.getStatus();
        if (!"CONFIRMED".equals(status) && !"REJECTED".equals(status)) {
            throw new ConditionsNotMetException("Статус должен быть CONFIRMED или REJECTED");
        }

        // 2. Получаем событие и проверяем инициатора
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Только инициатор события может менять статусы заявок");
        }

        // 3. Если модерация заявок отключена
        if (Boolean.FALSE.equals(event.getRequestModeration())) {
            throw new ConditionsNotMetException("Для этого события подтверждение заявок не требуется");
        }

        // 4. Получаем все заявки на событие
        List<ParticipationRequest> allRequests = participationRequestRepository.findAllByEventId(eventId);

        // 5. Проверяем существование запрошенных ID
        List<Long> allRequestIds = allRequests.stream()
                .map(ParticipationRequest::getId)
                .collect(Collectors.toList());

        List<Long> absentRequestIds = requestDto.getRequestIds().stream()
                .filter(id -> !allRequestIds.contains(id))
                .collect(Collectors.toList());

        if (!absentRequestIds.isEmpty()) {
            throw new EntityNotFoundException("Заявки на участие с id=" + absentRequestIds + " не найдены");
        }

        // 6. Фильтруем только запрошенные заявки
        List<ParticipationRequest> requestsToUpdate = allRequests.stream()
                .filter(pr -> requestDto.getRequestIds().contains(pr.getId()))
                .collect(Collectors.toList());

        // 7. Проверяем, что все заявки в статусе PENDING
        List<Long> notPendingRequests = requestsToUpdate.stream()
                .filter(pr -> !"PENDING".equals(pr.getStatus()))
                .map(ParticipationRequest::getId)
                .collect(Collectors.toList());

        if (!notPendingRequests.isEmpty()) {
            throw new ConditionsNotMetException("Заявки на участие в событии не находятся в состоянии ожидания подтверждения");
        }

        // 8. ПРОВЕРКА ЛИМИТА ПРИ ПОДТВЕРЖДЕНИИ
        if ("CONFIRMED".equals(status)) {
            // Проверяем лимит участников
            if (event.getParticipantLimit() > 0) {
                Long confirmedCount = participationRequestRepository.countByEventIdAndStatus(eventId, "CONFIRMED");
                if (confirmedCount == null) confirmedCount = 0L;

                long availableSlots = event.getParticipantLimit() - confirmedCount;

                // Если лимит уже достигнут
                if (availableSlots <= 0) {
                    throw new ConflictException("Лимит участников события уже достигнут");
                }

                // Если доступных мест меньше, чем запрошенных заявок
                if (availableSlots < requestsToUpdate.size()) {
                    // Подтверждаем первые availableSlots заявок
                    List<ParticipationRequest> toConfirm = requestsToUpdate.stream()
                            .limit(availableSlots)
                            .collect(Collectors.toList());
                    List<ParticipationRequest> toReject = requestsToUpdate.stream()
                            .skip(availableSlots)
                            .collect(Collectors.toList());

                    toConfirm.forEach(pr -> pr.setStatus("CONFIRMED"));
                    toReject.forEach(pr -> pr.setStatus("REJECTED"));

                    participationRequestRepository.saveAll(toConfirm);
                    participationRequestRepository.saveAll(toReject);

                    Set<ParticipationRequestDto> confirmed = toConfirm.stream()
                            .map(ParticipationRequestMapper::toParticipationRequestDto)
                            .collect(Collectors.toSet());
                    Set<ParticipationRequestDto> rejected = toReject.stream()
                            .map(ParticipationRequestMapper::toParticipationRequestDto)
                            .collect(Collectors.toSet());

                    log.info("Частичное подтверждение заявок: подтверждено={}, отклонено={}, eventId={}",
                            toConfirm.size(), toReject.size(), eventId);

                    return EventRequestStatusUpdateResult.builder()
                            .confirmedRequests(confirmed)
                            .rejectedRequests(rejected)
                            .build();
                }
            }
        }

        // 9. Стандартное обновление статусов
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
