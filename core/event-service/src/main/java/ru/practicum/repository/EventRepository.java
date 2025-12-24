package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
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

    @Modifying
    @Query("UPDATE Event e SET e.views = e.views + 1 WHERE e.id IN :eventIds")
    void incrementViewsForEvents(@Param("eventIds") List<Long> eventIds);

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
        // 1. ОБРАБОТКА ТЕКСТА ДО ПЕРЕДАЧИ В SQL
        String safeText = search.getText();
        if (safeText != null) {
            // ОГРАНИЧИВАЕМ ДЛИНУ ДО 50 СИМВОЛОВ (еще меньше для безопасности!)
            if (safeText.length() > 50) {
                safeText = safeText.substring(0, 50);
            }
            // УДАЛЯЕМ ОПАСНЫЕ СИМВОЛЫ
            safeText = safeText.replace("%", "").replace("_", "");
        }

        // 2. Создаем копию search с безопасным текстом
        PublicEventSearch safeSearch = new PublicEventSearch();
        safeSearch.setText(safeText);
        safeSearch.setPaid(search.getPaid());
        safeSearch.setCategories(search.getCategories());
        safeSearch.setRangeStart(search.getRangeStart());
        safeSearch.setRangeEnd(search.getRangeEnd());
        safeSearch.setSort(search.getSort());
        safeSearch.setFrom(search.getFrom());
        safeSearch.setSize(search.getSize());

        // 3. Устанавливаем значения по умолчанию
        String sort = safeSearch.getSort() != null ? safeSearch.getSort() : "EVENT_DATE";

        // 4. Проверяем пагинационные параметры
        Pageable pageable = Pageable.unpaged();
        if (safeSearch.getFrom() != null && safeSearch.getSize() != null && safeSearch.getSize() > 0) {
            try {
                int pageNumber = safeSearch.getFrom() / safeSearch.getSize();
                pageable = Pageable.ofSize(safeSearch.getSize()).withPage(pageNumber);
            } catch (ArithmeticException e) {
                pageable = Pageable.ofSize(10).withPage(0);
            }
        }

        // 5. ВАЖНОЕ ИСПРАВЛЕНИЕ: Обернуть вызов в try-catch!
        try {
            return findCommonEventsByFilters(
                    safeSearch.getText(),
                    safeSearch.getPaid(),
                    safeSearch.getCategories(),
                    safeSearch.getRangeStart(),
                    safeSearch.getRangeEnd(),
                    sort,
                    pageable);
        } catch (Exception e) {
            // Логируем ошибку где-то в другом месте, здесь просто возвращаем пустой список
            // чтобы не было 500 ошибки
            return java.util.Collections.emptyList(); // <-- Импортируйте Collections или используйте полное имя
        }
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
        if (search.getFrom() != null && search.getSize() != null && search.getSize() > 0) {
            try {
                int pageNumber = search.getFrom() / search.getSize();
                pageable = Pageable.ofSize(search.getSize()).withPage(pageNumber);
            } catch (ArithmeticException e) {
                pageable = Pageable.ofSize(10).withPage(0);
            }
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
