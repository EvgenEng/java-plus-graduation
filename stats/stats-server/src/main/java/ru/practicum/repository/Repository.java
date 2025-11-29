package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.RequestOutputDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface Repository extends JpaRepository<EndpointHit, Integer> {

    @Query(value = "select new ru.practicum.RequestOutputDto( h.app, h.uri, count(h.ip))" +
            " from EndpointHit h where h.timestamp >= ?1 and h.timestamp <= ?2 " +
            "group by h.app, h.uri " +
            "order by 3 desc")
    List<RequestOutputDto> getByNoUnique(LocalDateTime start, LocalDateTime end);

    @Query(value = "select new ru.practicum.RequestOutputDto(h.app, h.uri, count(distinct(h.ip)))" +
            " from EndpointHit h where h.timestamp >= ?1 and h.timestamp <= ?2 " +
            "group by h.app, h.uri " +
            "order by 3 desc")
    List<RequestOutputDto> getByUnique(LocalDateTime start, LocalDateTime end);

    @Query(value = "select new ru.practicum.RequestOutputDto( h.app, h.uri, count(h.ip))" +
            " from EndpointHit h where h.timestamp >= ?1 and h.timestamp <= ?2 and h.uri in ?3 " +
            "group by h.app, h.uri " +
            "order by 3 desc")
    List<RequestOutputDto> getByNoUniqueByUri(LocalDateTime start, LocalDateTime end, List<String> uri);

    @Query(value = "select new ru.practicum.RequestOutputDto( h.app, h.uri, count(distinct(h.ip)))" +
            " from EndpointHit h where h.timestamp >= ?1 and h.timestamp <= ?2 and h.uri in ?3 " +
            "group by h.app, h.uri " +
            "order by 3 desc")
    List<RequestOutputDto> getByUniqueByUri(LocalDateTime start, LocalDateTime end, List<String> uri);
}
