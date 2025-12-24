package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventInfoDto;

import java.util.List;

@FeignClient(name = "event-service", path = "/internal")
public interface EventServiceClient {

    @GetMapping("/events/{eventId}/info")
    EventInfoDto getEventInfo(@PathVariable("eventId") Long eventId);

    @GetMapping("/events/{eventId}/exists")
    Boolean checkEventExists(@PathVariable("eventId") Long eventId);

    @GetMapping("/events/category/{categoryId}")
    Boolean hasEventsWithCategory(@PathVariable("categoryId") Long categoryId);

    @GetMapping("/events/ids")
    List<EventInfoDto> getEventsInfoByIds(@RequestParam("eventIds") List<Long> eventIds);
}
