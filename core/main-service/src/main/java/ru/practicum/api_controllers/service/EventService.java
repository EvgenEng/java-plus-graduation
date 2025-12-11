package ru.practicum.api_controllers.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.client.EventClient;
import ru.practicum.entities.event.model.dto.EventDto;
import ru.practicum.entities.event.model.dto.UpdateAdminEventDto;
import ru.practicum.entities.event.model.dto.UpdateEventDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventClient eventClient;

    public List<EventDto> searchAdmin(ru.practicum.dto.AdminEventSearch search) {
        return eventClient.getAdminEvents(
                search.getUsers(),
                search.getStates(),
                search.getCategories(),
                search.getRangeStart(),
                search.getRangeEnd(),
                search.getFrom(),
                search.getSize()
        );
    }

    public EventDto updateByAdmin(Long eventId, UpdateAdminEventDto updateRequest) {
        return eventClient.updateEventByAdmin(eventId, updateRequest);
    }

    public List<EventDto> findByUserId(Long userId, Integer from, Integer size) {
        return eventClient.getEventsByUserId(userId, from, size);
    }

    public EventDto create(Long userId, EventDto newEventDto) {
        return eventClient.createEvent(userId, newEventDto);
    }

    public EventDto findByIdAndUser(Long userId, Long eventId) {
        return eventClient.getEventByIdAndUser(userId, eventId);
    }

    public EventDto updateByUser(Long userId, Long eventId, UpdateEventDto eventDto) {
        return eventClient.updateEventByUser(userId, eventId, eventDto);
    }

    public List<EventDto> searchCommon(ru.practicum.entities.event.model.dto.PublicEventSearch search) {
        return eventClient.searchEvents(
                search.getText(),
                search.getCategories(),
                search.getPaid(),
                search.getRangeStart(),
                search.getRangeEnd(),
                search.getSort().name(),
                search.getFrom(),
                search.getSize()
        );
    }

    public EventDto findById(Long eventId) {
        return eventClient.getEventById(eventId);
    }

    // Дополнительные методы для внутреннего использования
    public ru.practicum.dto.EventInfoDto getEventInfo(Long eventId) {
        return eventClient.getEventInfo(eventId);
    }

    public Boolean existsById(Long eventId) {
        return eventClient.checkEventExists(eventId);
    }

    public Boolean hasEventsWithCategory(Long categoryId) {
        return eventClient.hasEventsWithCategory(categoryId);
    }

    public List<ru.practicum.dto.EventInfoDto> getEventsInfoByIds(List<Long> eventIds) {
        return eventClient.getEventsInfoByIds(eventIds);
    }
}
