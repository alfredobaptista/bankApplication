package com.github.freddy.bankApi.repository;

import com.github.freddy.bankApi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByBi(String biNumber);

    @Query("SELECT u FROM User u " +
            "WHERE (:role IS NULL OR u.role = :role) " +
            "AND (:bi IS NULL OR u.bi LIKE %:bi%) " )
    Page<User> findByFilters(
            @Param("role") String role,
            @Param("bi") String bi,
            Pageable pageable
    );

}
