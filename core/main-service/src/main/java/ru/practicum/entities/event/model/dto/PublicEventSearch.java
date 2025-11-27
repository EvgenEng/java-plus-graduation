package ru.practicum.entities.event.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.entities.event.model.enums.EventSearchOrder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventSearch {
    private String text;

    private Boolean paid;

    private LocalDateTime rangeStart;

    private LocalDateTime rangeEnd;

    private List<Long> categories;

    private Boolean onlyAvailable;

    private EventSearchOrder sort;

    private Integer from;

    private Integer size;
}