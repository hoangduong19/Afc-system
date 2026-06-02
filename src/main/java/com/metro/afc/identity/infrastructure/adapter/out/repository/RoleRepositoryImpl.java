package com.metro.afc.identity.infrastructure.adapter.out.repository;

import com.metro.afc.identity.domain.model.Role;
import com.metro.afc.identity.domain.port.out.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository jpa;

    @Override
    public Optional<Role> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return jpa.findByCode(code);
    }

    @Override
    public List<Role> findAll() {
        return jpa.findAll();
    }
}
