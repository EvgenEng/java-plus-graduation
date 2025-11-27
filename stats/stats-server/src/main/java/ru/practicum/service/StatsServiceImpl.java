package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.RequestCreateDto;
import ru.practicum.RequestDto;
import ru.practicum.RequestOutputDto;
import ru.practicum.model.Mapper;
import ru.practicum.repository.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final Repository repository;

    @Override
    public RequestDto create(RequestCreateDto hitDtoCreate) {
        return Mapper.toRequestDto(repository.save(Mapper.toEndpointHit(hitDtoCreate)));
    }

    @Override
    public List<RequestOutputDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uri, boolean unique) {
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Старт не может быть позже конца");
        }
        List<RequestOutputDto> stats;
        boolean isNotBlankUri = (uri != null);

        if (isNotBlankUri) {
            if (unique) {
                stats = repository.getByUniqueByUri(start, end, uri);
            } else {
                stats = repository.getByNoUniqueByUri(start, end, uri);
            }
        } else {
            if (unique) {
                stats = repository.getByUnique(start, end);
            } else {
                stats = repository.getByNoUnique(start, end);
            }
        }
        return stats;
    }
}
