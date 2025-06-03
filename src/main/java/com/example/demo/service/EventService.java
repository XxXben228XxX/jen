package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors; // Додано для Collectors

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(String id) {
        return eventRepository.findById(id).orElse(null);
    }

    public Event addEvent(Event event) {
        // Перевірка на null дату перед встановленням UUID
        // Якщо ви хотіли валідацію дати, як у попередньому прикладі, її потрібно додати тут:
        // if (event.getDate() == null || event.getDate().before(new Date())) {
        //     throw new IllegalArgumentException("Event date cannot be in the past or null.");
        // }
        event.setId(UUID.randomUUID().toString());
        return eventRepository.save(event);
    }

    public Event updateEvent(String id, Event updatedEvent) {
        if (eventRepository.existsById(id)) {
            updatedEvent.setId(id);
            return eventRepository.save(updatedEvent);
        }
        return null;
    }

    public boolean deleteEvent(String id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Оновлена логіка getEventsByFilter для ефективності та тестування
    public List<Event> getEventsByFilter(String name, String location, String eventType) {
        // Якщо всі параметри null або порожні, повертаємо всі події
        if ((name == null || name.isEmpty()) &&
                (location == null || location.isEmpty()) &&
                (eventType == null || eventType.isEmpty())) {
            return eventRepository.findAll();
        }

        // Пріоритет за іменем (якщо ваш репозиторій має findByName)
        if (name != null && !name.isEmpty()) {
            return eventRepository.findByName(name); // Припускаємо, що такий метод існує
        }

        // Якщо ім'я не задано, шукаємо за типом (якщо ваш репозиторій має findByEventType)
        if (eventType != null && !eventType.isEmpty()) {
            return eventRepository.findByEventType(eventType); // Припускаємо, що такий метод існує
        }

        // Якщо задано лише локацію, або комбінацію без імен/типів, фільтруємо з усіх подій
        if (location != null && !location.isEmpty()) {
            return eventRepository.findAll().stream() // Отримуємо всі, потім фільтруємо
                    .filter(e -> e.getLocation() != null && e.getLocation().toLowerCase().contains(location.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Це не має бути досяжним, якщо верхні умови коректні, але для безпеки:
        return Collections.emptyList();
    }


    public double getTicketPrice(Event event) {
        double basePrice = 50.0;
        String eventType = event.getEventType();
        if ("Концерт".equals(eventType)) {
            return basePrice * 1.2;
        } else if ("Театр".equals(eventType)) {
            return basePrice * 1.1;
        } else {
            return basePrice;
        }
    }
}