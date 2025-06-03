package com.example.demo.controller;

import com.example.demo.repository.UserRepository;
import com.example.demo.security.SignupRequest; // Правильний імпорт для SignupRequest
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections; // Додано для Collections.singleton()
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Кожна транзакція буде відкочуватися після тесту
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUserSuccessfully() throws Exception {
        // 1. Підготовка даних для запиту реєстрації
        SignupRequest signupRequest = new SignupRequest(); // Використовуємо конструктор без аргументів
        signupRequest.setUsername("testuser_signup");
        signupRequest.setEmail("test_signup@example.com");
        signupRequest.setPassword("password123");
        // Замість setRole(null) використовуємо setRoles() і передаємо порожній Set або Set з роллю за замовчуванням
        signupRequest.setRoles(Collections.emptySet()); // Або Collections.singleton("user") якщо "user" роль за замовчуванням

        String jsonSignupRequest = objectMapper.writeValueAsString(signupRequest);

        // 2. Виконання HTTP-запиту до ендпоінта /api/auth/signup
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSignupRequest))
                .andExpect(status().isOk()) // або .isCreated(), залежить від реалізації контролера
                .andExpect(jsonPath("$.message").value("User registered successfully!")); // Перевіряємо повідомлення успіху

        // 3. Перевірка змін у базі даних
        assertThat(userRepository.findByUsername("testuser_signup")).isPresent();

        userRepository.findByUsername("testuser_signup").ifPresent(user -> {
            assertThat(user.getEmail()).isEqualTo("test_signup@example.com");
            // assertThat(user.getPassword()).isNotNull(); // Можна перевірити, що пароль не null, якщо його хешує контролер
        });
    }

    @Test
    void testRegisterDuplicateUsername() throws Exception {
        // Спочатку реєструємо користувача
        SignupRequest firstSignup = new SignupRequest(); // Використовуємо конструктор без аргументів
        firstSignup.setUsername("existinguser");
        firstSignup.setEmail("existing@example.com");
        firstSignup.setPassword("password");
        firstSignup.setRoles(Collections.emptySet()); // Або Collections.singleton("user")

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstSignup)))
                .andExpect(status().isOk());

        // Спроба зареєструвати того ж користувача знову
        SignupRequest duplicateSignup = new SignupRequest();
        duplicateSignup.setUsername("existinguser");
        duplicateSignup.setEmail("another@example.com");
        duplicateSignup.setPassword("password");
        duplicateSignup.setRoles(Collections.emptySet()); // Або Collections.singleton("user")

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateSignup)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        // Спочатку реєструємо користувача
        SignupRequest firstSignup = new SignupRequest();
        firstSignup.setUsername("user1");
        firstSignup.setEmail("duplicate@example.com");
        firstSignup.setPassword("password");
        firstSignup.setRoles(Collections.emptySet()); // Або Collections.singleton("user")

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstSignup)))
                .andExpect(status().isOk());

        // Спроба зареєструвати користувача з тим же email
        SignupRequest duplicateEmail = new SignupRequest();
        duplicateEmail.setUsername("user2");
        duplicateEmail.setEmail("duplicate@example.com");
        duplicateEmail.setPassword("password");
        duplicateEmail.setRoles(Collections.emptySet()); // Або Collections.singleton("user")

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmail)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));
    }
}