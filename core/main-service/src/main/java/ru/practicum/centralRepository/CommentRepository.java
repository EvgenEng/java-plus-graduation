package ru.practicum.centralRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.entities.comment.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM comments AS c " +
            "WHERE ((CAST(:rangeStart as DATE) IS NULL OR c.created >= :rangeStart) " +
            "AND (CAST(:rangeEnd as DATE) IS NULL OR c.created <= :rangeEnd)) " +
            "GROUP BY c.id " +
            "ORDER BY c.id ASC")
    List<Comment> findFilteredComments(
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @Query("SELECT c FROM comments AS c " +
            "WHERE :eventId = c.event.id " +
            "AND ((CAST(:rangeStart as DATE) IS NULL OR c.created >= :rangeStart) " +
            "AND (CAST(:rangeEnd as DATE) IS NULL OR c.created <= :rangeEnd)) " +
            "GROUP BY c.id " +
            "ORDER BY c.id ASC")
    List<Comment> findCommentsByEventIdAndDates(
            @Param("eventId") Long eventId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @Query("SELECT c FROM comments AS c " +
            "WHERE :userId = c.user.id " +
            "AND ((CAST(:rangeStart as DATE) IS NULL OR c.created >= :rangeStart) " +
            "AND (CAST(:rangeEnd as DATE) IS NULL OR c.created <= :rangeEnd)) " +
            "ORDER BY c.id ASC")
    List<Comment> findCommentsByUserIdAndDates(
            @Param("userId") Long userId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @Query("SELECT c.event.id, COUNT(c) FROM comments c WHERE c.event.id IN :eventIds GROUP BY c.event.id")
    List<Object[]> countByEventIdsGrouped(@Param("eventIds") List<Long> eventIds);
}