package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalRequestController {

    private final ParticipationRequestService participationRequestService;

    @GetMapping("/events/{eventId}/confirmed-count")
    public Long getConfirmedRequestsCount(@PathVariable Long eventId) {
        log.debug("Получение количества подтвержденных заявок для события: eventId={}", eventId);
        return participationRequestService.countConfirmedRequestsByEventId(eventId);
    }

    @GetMapping("/events/confirmed")
    public List<ParticipationRequestDto> getConfirmedRequestsByEventIds(@RequestParam List<Long> eventIds) {
        log.debug("Получение подтвержденных заявок для событий: eventIds={}", eventIds);
        return participationRequestService.getConfirmedRequestsByEventIds(eventIds);
    }
}
