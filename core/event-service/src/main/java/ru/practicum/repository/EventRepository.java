package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.AdminEventSearch;
import ru.practicum.dto.PublicEventSearch;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e " +
            "WHERE (:text IS NULL OR " +
            "      LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "      OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "      OR LOWER(e.title) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:categories IS NULL OR e.categoryId IN :categories) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "AND e.state = 'PUBLISHED' " +  // Только опубликованные
            "ORDER BY " +
            "CASE WHEN :sort = 'EVENT_DATE' THEN e.eventDate END ASC, " +
            "CASE WHEN :sort = 'VIEWS' THEN e.views END DESC")
    List<Event> findCommonEventsByFilters(
            @Param("text") String text,
            @Param("paid") Boolean paid,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("sort") String sort,
            Pageable pageable);

    default List<Event> findCommonEventsByFilters(PublicEventSearch search) {
        Pageable pageable = Pageable.unpaged();
        Integer from = search.getFrom();
        Integer size = search.getSize();
        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }

        String sort = search.getSort();
        if (sort == null) {
            sort = "EVENT_DATE";
        }

        return findCommonEventsByFilters(
                search.getText(),
                search.getPaid(),
                search.getCategories(),
                search.getRangeStart(),
                search.getRangeEnd(),
                sort,
                pageable);
    }

    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiatorId IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.categoryId IN :categories) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "ORDER BY e.eventDate DESC")
    List<Event> findAdminEventsByFilters(
            @Param("users") List<Long> users,
            @Param("states") List<String> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    default List<Event> findAdminEventsByFilters(AdminEventSearch search) {
        Pageable pageable = Pageable.unpaged();
        Integer from = search.getFrom();
        Integer size = search.getSize();
        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }
        return findAdminEventsByFilters(
                search.getUsers(),
                search.getStates(),
                search.getCategories(),
                search.getRangeStart(),
                search.getRangeEnd(),
                pageable);
    }

    List<Event> findAllByInitiatorIdOrderByEventDateDesc(Long initiatorId, Pageable pageable);

    default List<Event> findAllByInitiatorIdOrderByEventDateDesc(Long initiatorId, Integer from, Integer size) {
        if (from != null && size != null) {
            return findAllByInitiatorIdOrderByEventDateDesc(
                    initiatorId,
                    Pageable.ofSize(size).withPage(from / size));
        }
        return findAllByInitiatorIdOrderByEventDateDesc(initiatorId, Pageable.unpaged());
    }

    boolean existsByCategoryId(Long categoryId);

    List<Event> findAllByIdIn(List<Long> ids);

    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.state = 'PUBLISHED'")
    Event findPublishedById(@Param("eventId") Long eventId);

    List<Event> findByState(String state, Pageable pageable);
}
