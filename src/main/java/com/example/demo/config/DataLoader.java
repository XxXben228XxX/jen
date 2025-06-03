// src/main/java/com/example/demo/config/DataLoader.java (або com.example.demo.util/DataLoader.java)
package com.example.demo.config; // Або інший відповідний пакет

import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initDatabase(RoleRepository roleRepository) {
        return args -> {
            // Перевіряємо, чи існують ролі, і якщо ні - створюємо їх
            if (roleRepository.findByName(ERole.ROLE_USER.name()).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_USER.name()));
                System.out.println("Role ROLE_USER added.");
            }
            if (roleRepository.findByName(ERole.ROLE_MODERATOR.name()).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_MODERATOR.name()));
                System.out.println("Role ROLE_MODERATOR added.");
            }
            if (roleRepository.findByName(ERole.ROLE_ADMIN.name()).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_ADMIN.name()));
                System.out.println("Role ROLE_ADMIN added.");
            }
            System.out.println("Database roles checked/initialized.");
        };
    }
}