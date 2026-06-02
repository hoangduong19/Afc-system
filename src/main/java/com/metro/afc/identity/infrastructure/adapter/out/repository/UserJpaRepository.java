package com.metro.afc.identity.infrastructure.adapter.out.repository;

import com.metro.afc.identity.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<User, UUID> {
    Optional<User> findById(UUID id);
    @Query("SELECT u FROM User u WHERE u.username.value = :username")
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.email.value = :email")
    Optional<User> findByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username.value = :username")
    boolean existsByUsername(String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email.value = :email")
    boolean existsByEmail(String email);
}