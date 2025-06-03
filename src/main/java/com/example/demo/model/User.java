package com.example.demo.model; // Змініть на ваш пакет, якщо він інший

import com.example.demo.model.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email") // Додаємо унікальність для email
        })
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank // Додаємо валідацію
    @Size(max = 20) // Обмеження розміру
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank // Додаємо валідацію
    @Size(max = 50) // Обмеження розміру
    @Email // Валідація формату email
    @Column(unique = true, nullable = false) // email також має бути унікальним та обов'язковим
    private String email; // *** ДОДАНО ПОЛЕ EMAIL ***

    @NotBlank // Додаємо валідацію
    @Size(max = 120) // Обмеження розміру (для хешованого пароля)
    @Column(nullable = false)
    private String password;

    // У класі User
    @ManyToMany(fetch = FetchType.EAGER) // Змінено на EAGER
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // *** ДОДАНО КОНСТРУКТОР ДЛЯ РЕЄСТРАЦІЇ ***
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}