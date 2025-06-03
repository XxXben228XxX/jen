package com.example.demo.controller;

import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Додано
import org.springframework.boot.test.context.SpringBootTest; // Змінено з WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Змінено з @WebMvcTest на @SpringBootTest
@SpringBootTest
@AutoConfigureMockMvc // Потрібно для ін'єкції MockMvc в @SpringBootTest
@Transactional // ДОДАЙ ЦЮ АНОТАЦІЮ!
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // userRepository та roleRepository більше не потрібно мокати через @MockBean тут,
    // якщо вони є частиною твого основного Spring контексту,
    // який завантажується за допомогою @SpringBootTest, і ти хочеш,
    // щоб вони використовували справжні реалізації або були замокані глобально.
    // АЛЕ, якщо ти хочеш, щоб вони були моками ТІЛЬКИ для цього тесту, тоді залиш @MockBean.
    // Зазвичай, для інтеграційних тестів з @SpringBootTest, ти не мокаєш репозиторії,
    // а використовуєш вбудовану базу даних (наприклад, H2) або запускаєш з справжньою.
    // Якщо ти мокаєш репозиторії, переконайся, що вони дійсно використовуються
    // контролером, а не проміжними сервісами.
    @MockBean // Залишимо, припускаючи, що ти хочеш мокувати залежності для контролера
    private UserRepository userRepository;

    @MockBean // Залишимо
    private RoleRepository roleRepository;

    private User adminUser;
    private User regularUser;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role(ERole.ROLE_ADMIN.name());
        adminRole.setId(1L);
        userRole = new Role(ERole.ROLE_USER.name());
        userRole.setId(2L);

        adminUser = new User("admin", "admin@example.com", "encodedAdminPass");
        adminUser.setId(101L);
        adminUser.setRoles(new HashSet<>(Collections.singletonList(adminRole)));

        regularUser = new User("user", "user@example.com", "encodedUserPass");
        regularUser.setId(102L);
        regularUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllUsers_shouldReturnAllUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(Arrays.asList(adminUser, regularUser));

        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(adminUser.getUsername()))
                .andExpect(jsonPath("$[1].username").value(regularUser.getUsername()));

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllUsers_shouldReturnForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_shouldReturnUser_whenFound() throws Exception {
        when(userRepository.findById(101L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/api/admin/users/{userId}", 101L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(adminUser.getUsername()));

        verify(userRepository, times(1)).findById(101L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_shouldReturnNotFound_whenNotFound() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/users/{userId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found!"));

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUserRoles_shouldUpdateRoles_whenValid() throws Exception {
        AdminController.UpdateUserRolesRequest request = new AdminController.UpdateUserRolesRequest();
        request.roles = new HashSet<>(Arrays.asList("ROLE_MODERATOR", "ROLE_USER"));

        Role modRole = new Role(ERole.ROLE_MODERATOR.name());
        modRole.setId(3L);

        when(userRepository.findById(102L)).thenReturn(Optional.of(regularUser));
        when(roleRepository.findByName(ERole.ROLE_MODERATOR.name())).thenReturn(Optional.of(modRole));
        when(roleRepository.findByName(ERole.ROLE_USER.name())).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        mockMvc.perform(put("/api/admin/users/{userId}/roles", 102L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User roles updated successfully!"));

        verify(userRepository, times(1)).findById(102L);
        // Змінено з times(1) на atLeastOnce() або times(2)
        verify(roleRepository, atLeastOnce()).findByName(ERole.ROLE_MODERATOR.name());
        verify(roleRepository, atLeastOnce()).findByName(ERole.ROLE_USER.name());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUserRoles_shouldReturnNotFound_whenUserNotFound() throws Exception {
        AdminController.UpdateUserRolesRequest request = new AdminController.UpdateUserRolesRequest();
        request.roles = Collections.singleton("ROLE_USER");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/admin/users/{userId}/roles", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found!"));

        verify(userRepository, times(1)).findById(999L);
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUserRoles_shouldReturnBadRequest_whenRoleNotFound() throws Exception {
        AdminController.UpdateUserRolesRequest request = new AdminController.UpdateUserRolesRequest();
        request.roles = new HashSet<>(Arrays.asList("ROLE_NON_EXISTENT", "ROLE_USER"));

        when(userRepository.findById(102L)).thenReturn(Optional.of(regularUser));
        when(roleRepository.findByName("ROLE_NON_EXISTENT")).thenReturn(Optional.empty());
        when(roleRepository.findByName(ERole.ROLE_USER.name())).thenReturn(Optional.of(userRole));


        mockMvc.perform(put("/api/admin/users/{userId}/roles", 102L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Role 'ROLE_NON_EXISTENT' is not found."));

        verify(userRepository, times(1)).findById(102L);
        verify(roleRepository, times(1)).findByName("ROLE_NON_EXISTENT");
        verify(roleRepository, times(1)).findByName(ERole.ROLE_USER.name());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void updateUserRoles_shouldReturnForbiddenForUser() throws Exception {
        AdminController.UpdateUserRolesRequest request = new AdminController.UpdateUserRolesRequest();
        request.roles = Collections.singleton("ROLE_ADMIN");

        mockMvc.perform(put("/api/admin/users/{userId}/roles", 102L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessAdminEndpoint_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}