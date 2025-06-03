package com.example.demo.controller;

import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtils;
import com.example.demo.security.MessageResponse;
import com.example.demo.security.SignupRequest;
import com.example.demo.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthController authController;

    private SignupRequest signupRequest;
    private User newUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setRoles(Collections.singleton("user"));

        newUser = new User("testuser", "test@example.com", "encodedPassword");
        newUser.setId(1L);

        userRole = new Role();
        // --- ВИПРАВЛЕНО ТУТ ---
        userRole.setId(1L); // Використовуємо 1L, щоб вказати, що це значення типу Long
        // ----------------------
        userRole.setName(ERole.ROLE_USER.name());
    }

    @Test
    @DisplayName("should register user successfully when username and email are unique")
    void registerUser_success() {
        // Arrange
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER.name())).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        ResponseEntity<?> responseEntity = authController.registerUser(signupRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(new MessageResponse("User registered successfully!").getMessage(),
                ((MessageResponse) responseEntity.getBody()).getMessage());

        verify(userRepository, times(1)).existsByUsername(signupRequest.getUsername());
        verify(userRepository, times(1)).existsByEmail(signupRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(signupRequest.getPassword());
        verify(roleRepository, times(1)).findByName(ERole.ROLE_USER.name());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("should return bad request when username already exists")
    void registerUser_usernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

        // Act
        ResponseEntity<?> responseEntity = authController.registerUser(signupRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(new MessageResponse("Error: Username is already taken!").getMessage(),
                ((MessageResponse) responseEntity.getBody()).getMessage());

        verify(userRepository, times(1)).existsByUsername(signupRequest.getUsername());
        verify(userRepository, never()).existsByEmail(any(String.class));
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should return bad request when email already exists")
    void registerUser_emailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // Act
        ResponseEntity<?> responseEntity = authController.registerUser(signupRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(new MessageResponse("Error: Email is already in use!").getMessage(),
                ((MessageResponse) responseEntity.getBody()).getMessage());

        verify(userRepository, times(1)).existsByUsername(signupRequest.getUsername());
        verify(userRepository, times(1)).existsByEmail(signupRequest.getEmail());
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should assign default USER role when roles are not specified")
    void registerUser_defaultUserRole() {
        // Arrange
        signupRequest.setRoles(null);

        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER.name())).thenReturn(Optional.of(userRole));

        // Act
        ResponseEntity<?> responseEntity = authController.registerUser(signupRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(new MessageResponse("User registered successfully!").getMessage(),
                ((MessageResponse) responseEntity.getBody()).getMessage());

        verify(roleRepository, times(1)).findByName(ERole.ROLE_USER.name());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("should throw runtime exception when default USER role is not found")
    void registerUser_roleNotFound() {
        // Arrange
        signupRequest.setRoles(null);
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER.name())).thenReturn(Optional.empty());

        // Act & Assert
        try {
            authController.registerUser(signupRequest);
        } catch (RuntimeException e) {
            assertEquals("Error: Role 'ROLE_USER' is not found.", e.getMessage());
        }

        verify(userRepository, never()).save(any(User.class));
    }
}