package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    // ★★★★ ОБЯЗАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ РАБОТЫ RequestService ★★★★

    // 1. Найти все заявки по ID события
    List<ParticipationRequest> findByEventId(Long eventId);

    // 2. Найти все заявки по ID пользователя
    List<ParticipationRequest> findByRequesterId(Long requesterId);

    // 3. Проверить существование заявки по пользователю и событию
    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    // 4. Подсчитать количество заявок по событию и статусу
    Integer countByEventIdAndStatus(Long eventId, String status);

    // 5. Найти заявки по списку ID
    List<ParticipationRequest> findAllByIdIn(List<Long> ids);

    // 6. Найти заявку по пользователю и событию
    ParticipationRequest findByRequesterIdAndEventId(Long requesterId, Long eventId);

    // 7. Проверить, есть ли подтвержденная заявка у пользователя на событие
    @Query("SELECT COUNT(r) > 0 FROM ParticipationRequest r " +
            "WHERE r.eventId = :eventId AND r.requesterId = :userId AND r.status = 'CONFIRMED'")
    boolean hasUserConfirmedRequest(@Param("eventId") Long eventId, @Param("userId") Long userId);
}
