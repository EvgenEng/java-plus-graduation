package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM categories c ORDER BY c.id ASC")
    List<Category> findAllCategories(Pageable pageable);

    default List<Category> findAllCategories(Integer from, Integer size) {
        Pageable pageable = Pageable.unpaged();
        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }
        return findAllCategories(pageable);
    }

    @Query("SELECT COUNT(e) FROM Event e WHERE e.categoryId = :categoryId")
    Long countEventsByCategoryId(@Param("categoryId") Long categoryId);

    List<Category> findByNameIgnoreCase(String name);

    List<Category> findAllByIdIn(List<Long> ids);
}
