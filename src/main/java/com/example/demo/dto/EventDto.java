package com.example.demo.dto;

import lombok.Data; // Додаємо Lombok Data для автоматичної генерації геттерів/сеттерів/конструкторів
import lombok.NoArgsConstructor; // Додаємо для конструктора без аргументів
import lombok.AllArgsConstructor; // Додаємо для конструктора з усіма полями

import java.time.LocalDateTime; // Рекомендується використовувати LocalDateTime для дат
import java.util.Date; // Якщо ти все ж таки хочеш використовувати java.util.Date

@Data // Автоматично генерує геттери, сеттери, toString, equals, hashCode
@NoArgsConstructor // Генерує конструктор без аргументів
@AllArgsConstructor // Генерує конструктор з усіма полями
public class EventDto {
    private String name; // Змінено з 'title' на 'name'
    private String description;
    private String location;
    private String eventType;
    private String organizer; // Додано organizer, якщо він потрібен у DTO
    private Date date; // Якщо ти хочеш передавати Date напряму в DTO
    // Або
    // private LocalDateTime eventDate; // Якщо ти хочеш передавати LocalDateTime напряму в DTO
}