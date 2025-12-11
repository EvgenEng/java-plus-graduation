package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByEventIdAndRequesterId(Long eventId, Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.eventId = :eventId AND pr.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    List<ParticipationRequest> findAllByIdIn(List<Long> ids);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.eventId IN :eventIds AND pr.status = 'CONFIRMED'")
    List<ParticipationRequest> findConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);
}
