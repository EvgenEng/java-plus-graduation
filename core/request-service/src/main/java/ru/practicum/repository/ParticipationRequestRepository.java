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

    // Подсчет заявок по eventId и статусу
    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.eventId = :eventId AND pr.status = :status")
    Long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") String status);

    // Проверка существования заявки по eventId и userId
    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    // Найти заявки по eventId и статусу
    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, String status);

    // Найти заявки по userId и статусу
    List<ParticipationRequest> findByRequesterIdAndStatus(Long userId, String status);

    // Проверить, есть ли у пользователя подтвержденная заявка на событие
    @Query("SELECT CASE WHEN COUNT(pr) > 0 THEN true ELSE false END FROM ParticipationRequest pr WHERE pr.eventId = :eventId AND pr.requesterId = :userId AND pr.status = 'CONFIRMED'")
    boolean hasUserConfirmedRequestForEvent(@Param("eventId") Long eventId, @Param("userId") Long userId);
}
