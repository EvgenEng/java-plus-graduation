package ru.practicum.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.RequestCreateDto;
import ru.practicum.RequestDto;

import java.sql.Timestamp;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Mapper {

    public static EndpointHit toEndpointHit(RequestCreateDto requestCreateDto) {
        return EndpointHit.builder()
                .app(requestCreateDto.getApp())
                .ip(requestCreateDto.getIp())
                .timestamp(requestCreateDto.getTimestamp().toLocalDateTime())
                .uri(requestCreateDto.getUri())
                .build();
    }

    public static RequestDto toRequestDto(EndpointHit endpointHit) {
        return RequestDto.builder()
                .id((long) endpointHit.getId())
                .app(endpointHit.getApp())
                .ip(endpointHit.getIp())
                .timestamp(Timestamp.valueOf(endpointHit.getTimestamp()))
                .uri(endpointHit.getUri())
                .build();
    }
}
