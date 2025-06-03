package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data; // Припустимо, ви використовуєте Lombok
import lombok.NoArgsConstructor; // Якщо ви використовуєте @Data, то ймовірно @NoArgsConstructor
import lombok.EqualsAndHashCode; // Якщо використовуєте @Data, то ймовірно @EqualsAndHashCode

import java.util.HashSet;
import java.util.Set; // Для зв'язку OneToMany

@Entity
@Data // Генерує геттери, сеттери, toString, equals, hashCode
@NoArgsConstructor // Якщо ви використовуєте Lombok, це корисно
@EqualsAndHashCode(exclude = {"events"}) // Виключаємо "events" з equals/hashCode, щоб уникнути зациклення та проблем з Hibernate
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Якщо ID у вас Long

    private String name; // Приклад поля
    private String address; // Приклад поля
    // Додайте тут інші поля вашого Venue

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // "mappedBy = 'venue'" вказує, що поле 'venue' у класі Event є власником зв'язку.
    // cascade = CascadeType.ALL дозволяє каскадне збереження/видалення подій при операціях з Venue.
    // orphanRemoval = true видаляє події, якщо вони від'єднані від Venue.
    private Set<Event> events = new HashSet<>(); // Ініціалізуємо для уникнення NullPointerException

    // Методи для керування зв'язком (дуже важливо для двонаправлених зв'язків)
    public void addEvent(Event event) {
        if (events == null) {
            events = new HashSet<>();
        }
        events.add(event);
        event.setVenue(this); // Встановлюємо зворотній зв'язок
    }

    public void removeEvent(Event event) {
        if (events != null) {
            events.remove(event);
            event.setVenue(null); // Видаляємо зворотній зв'язок
        }
    }

    // Lombok @Data генерує геттери і сеттери,
    // але для колекцій і двонаправлених зв'язків краще мати явні add/remove методи.

    // Якщо ви не використовуєте Lombok @Data, вам потрібно буде додати ці геттери/сеттери вручну:
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; } // Зазвичай, setId для @GeneratedValue не потрібен, але Lombok його генерує

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Set<Event> getEvents() { // <<<< ЦЕЙ МЕТОД ТЕПЕР БУДЕ ДОСТУПНИЙ
        return events;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }
}