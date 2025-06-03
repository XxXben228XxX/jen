package com.example.demo.controller;

import com.example.demo.dto.EventDto;
import com.example.demo.model.Event;
import com.example.demo.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<Event> getEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable String id) { // ID тепер String
        Event event = eventService.getEventById(id);
        if (event != null) {
            return new ResponseEntity<>(event, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Event> addEvent(@RequestBody EventDto eventDto) {
        Event event = convertToEvent(eventDto);
        Event createdEvent = eventService.addEvent(event);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable String id, @RequestBody EventDto eventDto) { // ID тепер String
        Event event = convertToEvent(eventDto);
        Event updatedEvent = eventService.updateEvent(id, event);
        if (updatedEvent != null) {
            return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) { // ID тепер String
        boolean isDeleted = eventService.deleteEvent(id);
        if (isDeleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/price")
    public ResponseEntity<Double> getEventPrice(@PathVariable String id) { // ID тепер String
        Event event = eventService.getEventById(id);
        if (event != null) {
            double price = eventService.getTicketPrice(event);
            return new ResponseEntity<>(price, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Event>> getEventsByFilter(
            @RequestParam(required = false) String name, // Це вже відповідає полю name в Event
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String eventType) {
        List<Event> filteredEvents = eventService.getEventsByFilter(name, location, eventType);
        return new ResponseEntity<>(filteredEvents, HttpStatus.OK);
    }

    private Event convertToEvent(EventDto eventDto) {
        Event event = new Event();
        event.setName(eventDto.getName()); // *** ПОМИЛКА ВИПРАВЛЕНА ТУТ ***
        event.setDescription(eventDto.getDescription());
        event.setLocation(eventDto.getLocation());
        event.setEventType(eventDto.getEventType());
        event.setDate(eventDto.getDate() != null ? eventDto.getDate() : new Date()); // Використовуємо дату з DTO, або поточну
        event.setOrganizer(eventDto.getOrganizer()); // *** ДОДАНО для поля organizer ***
        return event;
    }
}