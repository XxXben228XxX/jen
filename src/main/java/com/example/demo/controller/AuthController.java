package com.example.demo.controller;

import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.RefreshToken; // Імпорт RefreshToken
import com.example.demo.repository.RefreshTokenRepository; // Імпорт RefreshTokenRepository
// Змінено імпорти, щоб вони були більш явними та уникнути "зіркових" імпортів
import com.example.demo.security.*;

import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Імпорт для @Value
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional; // Для Optional<RefreshToken>
import java.util.Set;
import java.util.UUID; // Для генерації унікального токена
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600) // Залиште "*" для розробки, для продакшну вкажіть конкретний URL
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenRepository refreshTokenRepository; // Додано RefreshTokenRepository

    @Value("${app.jwtRefreshExpirationMs}") // Додано для часу життя refresh токена
    private Long refreshTokenDurationMs;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs; // Час життя access токена

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(refreshToken -> {
                    // 1. Перевірка терміну дії refresh-токена
                    if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
                        refreshTokenRepository.delete(refreshToken); // Видаляємо прострочений токен
                        return ResponseEntity.badRequest().body(new MessageResponse("Refresh token was expired. Please make a new signin request."));
                    }

                    // 2. Refresh токен валідний, генеруємо новий access-токен
                    User user = refreshToken.getUser();
                    UserDetailsImpl userDetails = UserDetailsImpl.build(user); // Створюємо UserDetailsImpl з User
                    String newAccessToken = jwtUtils.generateJwtToken(userDetails);

                    // Опціонально: генерувати новий refresh-токен при кожному оновленні.
                    // Це підвищує безпеку, але потребує оновлення на клієнті.
                    // Наразі ми просто перевикористовуємо існуючий refresh-токен для простоти,
                    // але в реальному додатку варто подумати про ротацію.
                    // Для ротації refresh-токена потрібно:
                    // String newRefreshTokenString = UUID.randomUUID().toString();
                    // refreshToken.setToken(newRefreshTokenString);
                    // refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
                    // refreshTokenRepository.save(refreshToken); // Зберігаємо оновлений refresh-токен

                    List<String> roles = userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList());

                    return ResponseEntity.ok(new JwtResponse(
                            newAccessToken,
                            refreshToken.getToken(), // Повертаємо той самий (або новий, якщо ротуємо) refresh-токен
                            userDetails.getId(),
                            userDetails.getUsername(),
                            userDetails.getEmail(),
                            roles
                    ));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!")); // Якщо токен не знайдено
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails); // Ваш JwtUtils генерує токен від UserDetails

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // --- Логіка для Refresh Token ---
        String refreshTokenString = UUID.randomUUID().toString(); // Генеруємо унікальний refresh-токен
        Instant expiryDate = Instant.now().plusMillis(refreshTokenDurationMs);

        // Перевіряємо, чи існує вже refresh-токен для цього користувача
        Optional<RefreshToken> existingRefreshToken = refreshTokenRepository.findByUser(userRepository.findById(userDetails.getId()).orElse(null));

        RefreshToken refreshToken;
        if (existingRefreshToken.isPresent()) {
            refreshToken = existingRefreshToken.get();
            refreshToken.setToken(refreshTokenString); // Оновлюємо старий токен
            refreshToken.setExpiryDate(expiryDate); // Оновлюємо термін дії
        } else {
            // Якщо ні, створюємо новий
            refreshToken = new RefreshToken(
                    userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("User not found!")),
                    refreshTokenString,
                    expiryDate
            );
        }
        refreshTokenRepository.save(refreshToken); // Зберігаємо або оновлюємо refresh-токен в БД
        // --- Кінець логіки Refresh Token ---


        return ResponseEntity.ok(new JwtResponse(jwt,
                refreshToken.getToken(), // Передаємо refresh-токен у відповідь
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    // Метод registerUser залишається без змін (не залежить від refresh токенів)
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword()));

        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER.name())
                    .orElseThrow(() -> new RuntimeException("Error: Role 'ROLE_USER' is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN.name())
                                .orElseThrow(() -> new RuntimeException("Error: Role 'ROLE_ADMIN' is not found."));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR.name())
                                .orElseThrow(() -> new RuntimeException("Error: Role 'ROLE_MODERATOR' is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER.name())
                                .orElseThrow(() -> new RuntimeException("Error: Role 'ROLE_USER' is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}