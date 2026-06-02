package com.metro.afc.identity.domain.port.out.repository;

import com.metro.afc.identity.domain.model.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    Optional<Role> findById(UUID id);
    Optional<Role> findByCode(String code);
    List<Role> findAll();
}