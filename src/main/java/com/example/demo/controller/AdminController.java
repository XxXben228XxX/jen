package com.example.demo.controller;

import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Додано імпорт HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Всі методи в цьому контролері вимагають ролі ADMIN
public class AdminController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    // DTO для відповіді, щоб не повертати пароль
    public static class UserAdminResponse {
        public Long id;
        public String username;
        public String email;
        public List<String> roles;

        public UserAdminResponse(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.roles = user.getRoles().stream()
                    .map(Role::getName) // Припускаємо, що Role має getName()
                    .collect(Collectors.toList());
        }
    }

    // DTO для зміни ролей
    public static class UpdateUserRolesRequest {
        public Set<String> roles; // Список назв ролей, які потрібно встановити
    }


    @GetMapping("/users")
    public ResponseEntity<List<UserAdminResponse>> getAllUsers() {
        List<UserAdminResponse> users = userRepository.findAll().stream()
                .map(UserAdminResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            // Виправлення: Використовуємо .status(HttpStatus.NOT_FOUND).body(...)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found!"));
        }
        // Виправлення: Для ResponseEntity.ok() метод .body() приймає об'єкт
        return ResponseEntity.ok(new UserAdminResponse(userOptional.get()));
    }

    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<?> updateUserRoles(@PathVariable Long userId, @RequestBody UpdateUserRolesRequest request) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            // Виправлення: Використовуємо .status(HttpStatus.NOT_FOUND).body(...)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found!"));
        }

        User user = userOptional.get();
        Set<Role> newRoles = new HashSet<>();

        if (request.roles == null || request.roles.isEmpty()) {
            // Якщо ролі не вказані, можна скинути до USER або повернути помилку
            // Наприклад, встановлюємо ROLE_USER за замовчуванням
            Role userRole = roleRepository.findByName(ERole.ROLE_USER.name())
                    .orElseThrow(() -> new RuntimeException("Error: Role 'ROLE_USER' is not found."));
            newRoles.add(userRole);
        } else {
            for (String roleName : request.roles) { // Використовуємо звичайний for-each для обробки можливих винятків
                Optional<Role> role = roleRepository.findByName(roleName);
                if (role.isPresent()) {
                    newRoles.add(role.get());
                } else {
                    // Якщо роль не знайдена, повертаємо помилку 400 Bad Request
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: Role '" + roleName + "' is not found."));
                }
            }
        }

        user.setRoles(newRoles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User roles updated successfully!"));
    }
}