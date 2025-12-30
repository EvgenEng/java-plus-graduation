package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.AdminEventSearch;
import ru.practicum.dto.PublicEventSearch;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 1. Найти событие по ID с проверкой инициатора
    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.initiatorId = :userId")
    Event findByIdAndInitiatorId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    // 2. Проверить существование события по ID и инициатору
    boolean existsByIdAndInitiatorId(Long eventId, Long userId);

    // 3. Получить событие с минимальной информацией
    @Query("SELECT e.id, e.initiatorId, e.state, e.participantLimit, e.requestModeration FROM Event e WHERE e.id = :eventId")
    Object[] findEventInfoById(@Param("eventId") Long eventId);

    /*@Modifying
    @Query("UPDATE Event e SET e.views = e.views + 1 WHERE e.id IN :eventIds")
    void incrementViewsForEvents(@Param("eventIds") List<Long> eventIds);

    // 4. Публичный поиск событий
    @Query("SELECT e FROM Event e " +
            "WHERE (:text IS NULL OR " +
            "      LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "      OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "      OR LOWER(e.title) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:categories IS NULL OR e.categoryId IN :categories) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "AND e.state = 'PUBLISHED' " +
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
    */

    @Modifying
    @Query("UPDATE Event e SET e.views = e.views + 1 WHERE e.id IN :eventIds AND e.state = 'PUBLISHED'")
    void incrementViewsForEvents(@Param("eventIds") List<Long> eventIds);

    @Query("SELECT e FROM Event e " +
            "WHERE (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.title) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:categories IS NULL OR e.categoryId IN :categories) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "AND e.state = 'PUBLISHED' " +
            "ORDER BY " +
            "CASE WHEN :sort = 'EVENT_DATE' THEN e.eventDate END ASC, " +
            "CASE WHEN :sort = 'VIEWS' THEN e.eventDate END ASC, " +
            "e.id ASC")
    List<Event> findCommonEventsByFilters(
            @Param("text") String text,
            @Param("paid") Boolean paid,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("sort") String sort,
            Pageable pageable);

    // 5. Админский поиск событий
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

    // 6. Default метод для админского поиска
    default List<Event> findAdminEventsByFilters(AdminEventSearch search) {
        if (search.getSize() == null || search.getSize() <= 0) {
            search.setSize(10);
        }
        if (search.getFrom() == null || search.getFrom() < 0) {
            search.setFrom(0);
        }

        PageRequest pageRequest = PageRequest.of(
                search.getFrom() / search.getSize(),
                search.getSize()
        );

        return findAdminEventsByFilters(
                search.getUsers(),
                search.getStates(),
                search.getCategories(),
                search.getRangeStart(),
                search.getRangeEnd(),
                pageRequest
        );
    }

    List<Event> findAllByInitiatorIdOrderByEventDateDesc(Long initiatorId, Pageable pageable);

    default List<Event> findAllByInitiatorIdOrderByEventDateDesc(Long initiatorId, Integer from, Integer size) {
        if (from != null && size != null && size > 0) {
            try {
                int pageNumber = from / size;
                return findAllByInitiatorIdOrderByEventDateDesc(
                        initiatorId,
                        Pageable.ofSize(size).withPage(pageNumber));
            } catch (ArithmeticException e) {
            }
        }
        return findAllByInitiatorIdOrderByEventDateDesc(initiatorId, Pageable.unpaged());
    }

    boolean existsByCategoryId(Long categoryId);

    List<Event> findAllByIdIn(List<Long> ids);

    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.state = 'PUBLISHED'")
    Event findPublishedById(@Param("eventId") Long eventId);

    List<Event> findByState(String state, Pageable pageable);
}
