package ru.practicum.entities.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.centralRepository.EventRepository;
import ru.practicum.centralRepository.ParticipationRequestRepository;
import ru.practicum.centralRepository.UserRepository;
import ru.practicum.entities.event.model.Event;
import ru.practicum.entities.event.model.enums.EventState;
import ru.practicum.entities.request.model.ParticipationRequest;
import ru.practicum.entities.request.model.ParticipationRequestStatus;
import ru.practicum.entities.request.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.entities.request.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.entities.request.model.dto.ParticipationRequestDto;
import ru.practicum.entities.request.model.mapper.ParticipationRequestMapper;
import ru.practicum.entities.user.model.User;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipationRequestService {
    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public List<ParticipationRequestDto> getAllByUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        return participationRequestRepository.findAllByRequesterId(userId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto).toList();
    }

    public List<ParticipationRequestDto> getAllByEventAndInitiator(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Заявки на участие в событии может просмотреть только создатель события");
        }

        return participationRequestRepository.findAllByEventId(eventId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .toList();
    }

    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User requester = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Нельзя заявить участие в собственном событии");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Нельзя заявить участие в неопубликованном событии");
        }
        if (!participationRequestRepository.findAllByEventIdAndRequesterId(eventId, userId).isEmpty()) {
            throw new ConditionsNotMetException("Нельзя отправить дублирующую заявку на участие в событии");
        }
        if (event.getParticipantLimit() != 0 && Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
            throw new ConditionsNotMetException("Достигнут лимит заявок на участие в событии");
        }

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requester(requester)
                .event(event)
                .status(event.getParticipantLimit() > 0 && event.getRequestModeration() ? ParticipationRequestStatus.PENDING : ParticipationRequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();
        if (!event.getRequestModeration()) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        return ParticipationRequestMapper.toParticipationRequestDto(participationRequestRepository.save(participationRequest));
    }

    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка с id=" + requestId + " не найдена"));
        if (!participationRequest.getRequester().getId().equals(userId)) {
            throw new ConditionsNotMetException("Заявку на участие в событии можно отменить только пользователем, который её отправил");
        }

        Event event = eventRepository.findById(participationRequest.getEvent().getId())
                .orElseThrow(() -> new NotFoundException("Событие с id=" + participationRequest.getEvent().getId() + " не найдено"));

        if (participationRequest.getStatus() == ParticipationRequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        participationRequest.setStatus(ParticipationRequestStatus.CANCELED);
        participationRequestRepository.save(participationRequest);

        return ParticipationRequestMapper.toParticipationRequestDto(participationRequest);
    }

    @Transactional
    public EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest requestDto) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        validateEventOwnership(userId, event);
        if (event.getParticipantLimit() == 0) {
            throw new ConditionsNotMetException("Нельзя обновить статус заявок на участие в событии с отключенной модерацией заявок");
        }

        List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByEventId(eventId);
        List<Long> absentRequestIds = new ArrayList<>();
        validateRequestIds(participationRequests, requestDto.getRequestIds(), absentRequestIds);

        List<ParticipationRequest> participationRequestsToUpdate = participationRequests.stream()
                .filter(participationRequest -> requestDto.getRequestIds().contains(participationRequest.getId()))
                .toList();

        validatePendingStatuses(participationRequestsToUpdate);

        if (requestDto.getStatus() == ParticipationRequestStatus.CONFIRMED) {
            confirmRequests(event, participationRequestsToUpdate, participationRequests);
        } else if (requestDto.getStatus() == ParticipationRequestStatus.REJECTED) {
            rejectRequests(participationRequestsToUpdate, event);
        }

        participationRequests = participationRequestRepository.findAllByEventId(eventId);
        return buildResult(participationRequests);
    }

    // Проверка прав доступа
    private void validateEventOwnership(Long userId, Event event) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Заявки на участие в событии может обновить только создатель события");
        }
    }

    // Проверка наличия заявок
    private void validateRequestIds(List<ParticipationRequest> participationRequests, List<Long> requestIds, List<Long> absentRequestIds) {
        requestIds.forEach(id -> {
            if (!participationRequests.stream().map(ParticipationRequest::getId).toList().contains(id)) {
                absentRequestIds.add(id);
            }
        });

        if (!absentRequestIds.isEmpty()) {
            throw new NotFoundException("Заявки на участие с id=" + absentRequestIds + " не найдены");
        }
    }

    // Проверка статусов заявок
    private void validatePendingStatuses(List<ParticipationRequest> participationRequestsToUpdate) {
        List<Long> notPendingRequests = participationRequestsToUpdate.stream()
                .filter(participationRequest -> participationRequest.getStatus() != ParticipationRequestStatus.PENDING)
                .map(ParticipationRequest::getId)
                .toList();

        if (!notPendingRequests.isEmpty()) {
            throw new ConditionsNotMetException("Заявки на участие в событии не находятся в состоянии ожидания подтверждения");
        }
    }

    // логика подтверждения заявок
    private void confirmRequests(Event event, List<ParticipationRequest> participationRequestsToUpdate, List<ParticipationRequest> allRequests) {
        if (event.getConfirmedRequests() + participationRequestsToUpdate.size() > event.getParticipantLimit()) {
            throw new ConditionsNotMetException("Нельзя подтвердить заявки на участие в событии, так как превышен лимит заявок");
        }

        participationRequestsToUpdate.forEach(participationRequest -> participationRequest.setStatus(ParticipationRequestStatus.CONFIRMED));
        participationRequestRepository.saveAll(allRequests);

        event.setConfirmedRequests(event.getConfirmedRequests() + participationRequestsToUpdate.size());
        eventRepository.save(event);

        if (Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
            List<ParticipationRequest> requestsToReject = allRequests.stream()
                    .filter(participationRequest -> !participationRequestsToUpdate.contains(participationRequest))
                    .toList();

            requestsToReject.forEach(participationRequest -> participationRequest.setStatus(ParticipationRequestStatus.REJECTED));
            participationRequestRepository.saveAll(requestsToReject);
        }
    }

    // логика отклонения заявок
    private void rejectRequests(List<ParticipationRequest> participationRequestsToUpdate, Event event) {
        long confirmedRequestsToReject = participationRequestsToUpdate.stream()
                .filter(pr -> pr.getStatus() == ParticipationRequestStatus.CONFIRMED)
                .count();

        participationRequestsToUpdate.forEach(participationRequest ->
                participationRequest.setStatus(ParticipationRequestStatus.REJECTED));
        participationRequestRepository.saveAll(participationRequestsToUpdate);

        if (confirmedRequestsToReject > 0) {
            event.setConfirmedRequests(event.getConfirmedRequests() - confirmedRequestsToReject);
            eventRepository.save(event);
        }
    }

    // логика формирования результата
    private EventRequestStatusUpdateResult buildResult(List<ParticipationRequest> participationRequests) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(participationRequests.stream()
                        .filter(participationRequest -> participationRequest.getStatus() == ParticipationRequestStatus.CONFIRMED)
                        .map(ParticipationRequestMapper::toParticipationRequestDto)
                        .collect(Collectors.toSet()))
                .rejectedRequests(participationRequests.stream()
                        .filter(participationRequest -> participationRequest.getStatus() == ParticipationRequestStatus.REJECTED)
                        .map(ParticipationRequestMapper::toParticipationRequestDto)
                        .collect(Collectors.toSet()))
                .build();
    }
}