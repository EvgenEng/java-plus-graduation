package ru.practicum.centralRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.entities.event.model.Event;
import ru.practicum.entities.event.model.dto.AdminEventSearch;
import ru.practicum.entities.event.model.dto.PublicEventSearch;
import ru.practicum.entities.event.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM events e " +
            "WHERE (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.title) LIKE LOWER(CONCAT('%', :text, '%')) OR :text IS NULL) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND ((CAST(:rangeStart as DATE) IS NULL AND CAST(:rangeEnd as DATE) IS NULL AND e.eventDate > :currentTime) " +
            "OR (CAST(:rangeStart as DATE) IS NOT NULL AND e.eventDate >= :rangeStart) " +
            "OR (CAST(:rangeEnd as DATE) IS NOT NULL AND e.eventDate <= :rangeEnd)) " +
            "AND (:onlyAvailable IS NULL OR " +
            "     (:onlyAvailable = TRUE AND (e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)) " +
            "     OR :onlyAvailable = FALSE) " +
            "AND e.state = :state " +
            "ORDER BY " +
            "CASE WHEN :sort = 'EVENT_DATE' THEN e.eventDate END ASC")
    List<Event> findCommonEventsByFilters(
            @Param("text") String text,
            @Param("paid") Boolean paid,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            @Param("sort") String sort,
            @Param("state") EventState state,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);

    default List<Event> findCommonEventsByFilters(PublicEventSearch publicEventSearch) {
        Pageable pageable = Pageable.unpaged();
        Integer from = publicEventSearch.getFrom();
        Integer size = publicEventSearch.getSize();
        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }
        return findCommonEventsByFilters(
                publicEventSearch.getText(),
                publicEventSearch.getPaid(),
                publicEventSearch.getCategories(),
                publicEventSearch.getRangeStart(),
                publicEventSearch.getRangeEnd(),
                publicEventSearch.getOnlyAvailable(),
                publicEventSearch.getSort().name(),
                EventState.PUBLISHED,
                LocalDateTime.now(),
                pageable);
    }

    @Query("SELECT e FROM events e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (CAST(:rangeStart as DATE) IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (CAST(:rangeEnd as DATE) IS NULL OR e.eventDate <= :rangeEnd) " +
            "ORDER BY e.eventDate DESC")
    List<Event> findAdminEventsByFilters(
            @Param("users") List<Long> users,
            @Param("states") List<String> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    default List<Event> findAdminEventsByFilters(AdminEventSearch adminEventSearch) {
        Pageable pageable = Pageable.unpaged();
        Integer from = adminEventSearch.getFrom();
        Integer size = adminEventSearch.getSize();
        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }
        return findAdminEventsByFilters(
                adminEventSearch.getUsers(),
                adminEventSearch.getStates(),
                adminEventSearch.getCategories(),
                adminEventSearch.getRangeStart(),
                adminEventSearch.getRangeEnd(),
                pageable
        );
    }

    List<Event> findAllByInitiatorIdOrderByEventDateDesc(@Param("user") Long userId, Pageable pageable);

    default List<Event> findAllByInitiatorIdOrderByEventDateDesc(Long userId, Integer from, Integer size) {
        if (from != null && size != null) {
            return findAllByInitiatorIdOrderByEventDateDesc(userId, Pageable.ofSize(size).withPage(from / size));
        }
        return findAllByInitiatorIdOrderByEventDateDesc(userId, Pageable.unpaged());
    }

    List<Event> findAllByCategoryId(Long categoryId);

    List<Event> findAllByIdIn(List<Long> list);
}