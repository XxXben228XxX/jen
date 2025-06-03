// src/main/java/com/example/demo/security/LoginRequest.java
package com.example.demo.security; // Змініть на com.example.demo.dto, якщо ви переміщуєте DTO в dto

import jakarta.validation.constraints.NotBlank;
import lombok.Data; // Переконайтеся, що Lombok додано до вашого pom.xml

@Data // Генерує геттери, сеттери, equals, hashCode, toString
public class LoginRequest {
    @NotBlank(message = "Username cannot be blank") // Додайте повідомлення для кращої валідації
    private String username;

    @NotBlank(message = "Password cannot be blank") // Додайте повідомлення для кращої валідації
    private String password;
}