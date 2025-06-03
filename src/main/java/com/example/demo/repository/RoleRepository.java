// src/main/java/com/example/demo/repository/RoleRepository.java
package com.example.demo.repository;

import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // ЗМІНЕНО: Метод findByName тепер приймає String,
    // оскільки поле 'name' у сутності Role є String.
    Optional<Role> findByName(String name); // <-- Змінили ERole на String
}