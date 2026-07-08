package com.example.security.security_app.repositories;

import com.example.security.security_app.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByIsDeletedFalse();
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByResetToken(String token);

    @Query("SELECT CASE WHEN u.isActive = true AND u.isVerified = true AND u.isLocked = false AND u.isDeleted = false" +
            " THEN true ELSE false END " +
            "FROM User u WHERE u.username = :username")
    boolean isUserActiveAndUnlocked(@Param("username") String username);
}
