package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant; // Використовуємо Instant для роботи з датами та часом

@Entity
@Table(name = "refresh_tokens") // Назва таблиці в базі даних
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Зв'язок ManyToOne з User: один користувач може мати багато refresh-токенів
    // (хоча зазвичай обмежують одним активним токеном на користувача)
    @OneToOne // Changed to OneToOne based on common practice for refresh tokens
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false, unique = true)
    private String token; // Сам refresh-токен (довгий, випадково згенерований рядок)

    @Column(nullable = false)
    private Instant expiryDate; // Термін дії refresh-токена

    // Конструктор для зручності
    public RefreshToken(User user, String token, Instant expiryDate) {
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
    }
}