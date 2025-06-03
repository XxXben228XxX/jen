// ... (Твій існуючий код Event.java)
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // Додаємо цей імпорт для конструктора без аргументів
import lombok.AllArgsConstructor; // Додаємо цей імпорт для конструктора з усіма аргументами
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;
import java.time.ZoneId;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor // Цей конструктор включає УСІ поля
@EntityListeners(AuditingEntityListener.class)
public class Event {
    @Id
    private String id;
    private String name; // Це поле використовується замість "title"
    private String description;
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Column(name = "location")
    private String location;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "organizer") // Додано поле organizer
    private String organizer;

    // Геттери та сеттери для Date (конвертація між Date та LocalDateTime)
    public Date getDate() {
        if (eventDate != null) {
            return Date.from(eventDate.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    public void setDate(Date date) {
        if (date != null) {
            this.eventDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else {
            this.eventDate = null;
        }
    }

    // setVenue метод
    public void setVenue(Venue venue) {
        this.venue = venue;
        if (venue != null && !venue.getEvents().contains(this)) {
            venue.addEvent(this);
        }
    }
}