package ru.practicum.service;

import ru.practicum.RequestCreateDto;
import ru.practicum.RequestDto;
import ru.practicum.RequestOutputDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    RequestDto create(RequestCreateDto requestCreateDto);

    List<RequestOutputDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uri, boolean unique);
}
