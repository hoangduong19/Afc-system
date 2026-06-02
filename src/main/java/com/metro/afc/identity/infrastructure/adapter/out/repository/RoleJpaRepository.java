package com.metro.afc.identity.infrastructure.adapter.out.repository;

import com.metro.afc.identity.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleJpaRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByCode(String code);
}