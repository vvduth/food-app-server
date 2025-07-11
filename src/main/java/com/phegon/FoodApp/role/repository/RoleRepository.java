package com.phegon.FoodApp.role.repository;

import com.phegon.FoodApp.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // This interface will inherit all CRUD operations from JpaRepository
    // Additional query methods can be defined here if needed
    Optional<Role> findByName(String name); // Find a role by its name
}
