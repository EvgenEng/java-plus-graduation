package ru.practicum.centralRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.entities.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM users u " +
            "WHERE (:ids IS NULL OR u.id IN :ids) " +
            "ORDER BY u.id ASC")
    List<User> findUsers(@Param("ids") List<Long> ids, Pageable pageable);

    default List<User> findUsers(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = Pageable.unpaged();
        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }
        return findUsers(ids, pageable);
    }

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);
}