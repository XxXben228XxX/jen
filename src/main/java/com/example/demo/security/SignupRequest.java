package com.example.demo.security;

import java.util.Set;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data; // Додайте, якщо використовуєте Lombok

@Data // Анотація Lombok для автоматичної генерації геттерів, сеттерів, equals, hashCode та toString
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    private Set<String> roles; // Назви ролей (наприклад, "admin", "user")

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
}