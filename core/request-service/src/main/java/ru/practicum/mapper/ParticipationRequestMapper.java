package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParticipationRequestMapper {

    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
        return ParticipationRequestDto.builder()
                .id(participationRequest.getId())
                .requester(participationRequest.getRequesterId())
                .event(participationRequest.getEventId())
                .status(participationRequest.getStatus())
                .created(participationRequest.getCreated())
                .build();
    }

    public static ParticipationRequest toParticipationRequest(Long requesterId, Long eventId, String status) {
        return ParticipationRequest.builder()
                .requesterId(requesterId)
                .eventId(eventId)
                .status(status)
                .created(LocalDateTime.now())
                .build();
    }
}
