package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    Page<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);

    @Query("SELECT c FROM Compilation c WHERE LOWER(c.title) = LOWER(:title)")
    List<Compilation> findByTitleIgnoreCase(@Param("title") String title);

    @Query("SELECT c FROM Compilation c " +
            "WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    List<Compilation> findCompilations(@Param("pinned") Boolean pinned,
                                       Pageable pageable);

    default List<Compilation> findCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = Pageable.unpaged();
        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }
        return findCompilations(pinned, pageable);
    }
}
