package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.entity.Role;

import java.util.Optional;

public interface RoleService {
    void saveAll(Iterable<Role> roles);

    Optional<Role> findByName(String name);

    Optional<Role> getDefaultRole();
}
