package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c ORDER BY c.id ASC")
    List<Category> findAllCategories(Pageable pageable);

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.categoryId = :categoryId")
    boolean existsByCategoryId(@Param("categoryId") Long categoryId);

    default List<Category> findAllCategories(Integer from, Integer size) {
        Pageable pageable = Pageable.unpaged();
        if (from != null && size != null && size > 0) {
            try {
                int pageNumber = from / size;
                pageable = Pageable.ofSize(size).withPage(pageNumber);
            } catch (ArithmeticException e) {
                pageable = Pageable.ofSize(10).withPage(0);
            }
        }
        return findAllCategories(pageable);
    }

    List<Category> findByNameIgnoreCase(String name);

    List<Category> findAllByIdIn(List<Long> ids);
}
