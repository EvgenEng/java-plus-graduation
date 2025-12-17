package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventShortDto;

import java.util.List;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/internal/events/exists")
    List<Long> getExistingEventIds(@RequestParam("eventIds") List<Long> eventIds);

    @GetMapping("/internal/events/short")
    List<EventShortDto> getEventsShortByIds(@RequestParam("eventIds") List<Long> eventIds);
}
