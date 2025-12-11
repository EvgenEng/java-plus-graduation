package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c " +
            "WHERE (:rangeStart IS NULL OR c.created >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR c.created <= :rangeEnd) " +
            "ORDER BY c.created DESC")
    List<Comment> findFilteredComments(
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @Query("SELECT c FROM Comment c " +
            "WHERE c.eventId = :eventId " +
            "AND (:rangeStart IS NULL OR c.created >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR c.created <= :rangeEnd) " +
            "ORDER BY c.created DESC")
    List<Comment> findCommentsByEventIdAndDates(
            @Param("eventId") Long eventId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @Query("SELECT c FROM Comment c " +
            "WHERE c.userId = :userId " +
            "AND (:rangeStart IS NULL OR c.created >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR c.created <= :rangeEnd) " +
            "ORDER BY c.created DESC")
    List<Comment> findCommentsByUserIdAndDates(
            @Param("userId") Long userId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
