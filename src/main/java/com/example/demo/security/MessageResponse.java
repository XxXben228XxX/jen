package com.example.demo.security;

import lombok.AllArgsConstructor; // Для конструктора з усіма аргументами
import lombok.Data; // Генерує геттери, сеттери, equals, hashCode, toString
import lombok.NoArgsConstructor; // Для конструктора без аргументів

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private String message;
}
