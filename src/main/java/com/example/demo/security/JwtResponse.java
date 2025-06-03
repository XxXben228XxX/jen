package com.example.demo.security;

import java.util.List;
import lombok.Data; // Додайте, якщо використовуєте Lombok
import lombok.NoArgsConstructor; // Для конструктора без аргументів, якщо потрібно

@Data // Анотація Lombok
@NoArgsConstructor // Додано для зручності
public class JwtResponse {
    private String accessToken;
    private String refreshToken; // Додано поле для refresh-токена
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private List<String> roles;

    // Оновлений конструктор для включення refreshToken
    public JwtResponse(String accessToken, String refreshToken, Long id, String username, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken; // Ініціалізуємо refreshToken
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}