package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock // Мок-об'єкт для залежності EventRepository
    private EventRepository eventRepository;

    @InjectMocks // Екземпляр EventService, куди будуть ін'єктовані моки
    private EventService eventService;

    private Event validEvent;
    private Event concertEvent;
    private Event theaterEvent;
    private Event otherEvent;


    @BeforeEach
    void setUp() {
        validEvent = new Event();
        validEvent.setId("123e4567-e89b-12d3-a456-426614174000"); // Приклад UUID
        validEvent.setName("Summer Festival");
        validEvent.setDescription("Music and fun");
        validEvent.setLocation("Central Park");
        validEvent.setEventType("Concert");
        validEvent.setDate(Date.from(LocalDateTime.now().plusDays(10).atZone(ZoneId.systemDefault()).toInstant()));
        validEvent.setOrganizer("Organizer Co.");

        // Для getTicketPrice
        concertEvent = new Event();
        concertEvent.setEventType("Концерт");

        theaterEvent = new Event();
        theaterEvent.setEventType("Театр");

        otherEvent = new Event();
        otherEvent.setEventType("Виставка"); // Або будь-який інший тип
    }

    // Тест на успішне виконання методу за типових умов.
    @Test
    void addEvent_shouldReturnSavedEvent_whenValid() {
        // Arrange
        Event eventToSave = new Event();
        eventToSave.setName("New Event");
        eventToSave.setDescription("Desc");
        eventToSave.setLocation("Loc");
        eventToSave.setEventType("Type");
        eventToSave.setDate(new Date());

        // Мокуємо, що repository.save повертає вхідний об'єкт, але з встановленим ID
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event argEvent = invocation.getArgument(0);
            return argEvent;
        });

        // Act
        Event result = eventService.addEvent(eventToSave);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull(); // Перевіряємо, що ID був встановлений
        assertThat(result.getName()).isEqualTo("New Event");
        // Перевірка, що метод save був викликаний рівно один раз з об'єктом Event
        verify(eventRepository, times(1)).save(any(Event.class));
    }


    // Тест на обробку однієї з можливих виняткових ситуацій.
    @Test
    void updateEvent_shouldReturnNull_whenEventNotFound() {
        // Arrange
        String nonExistentId = "non-existent-id";
        when(eventRepository.existsById(nonExistentId)).thenReturn(false);

        // Act
        Event result = eventService.updateEvent(nonExistentId, new Event());

        // Assert
        assertNull(result); // Очікуємо null, оскільки подія не знайдена
        verify(eventRepository, times(1)).existsById(nonExistentId);
        verify(eventRepository, never()).save(any(Event.class)); // save не має бути викликаний
    }

    @Test
    void deleteEvent_shouldReturnFalse_whenEventNotFound() {
        // Arrange
        String nonExistentId = "non-existent-id";
        when(eventRepository.existsById(nonExistentId)).thenReturn(false);

        // Act
        boolean result = eventService.deleteEvent(nonExistentId);

        // Assert
        assertFalse(result); // Очікуємо false, оскільки подія не знайдена
        verify(eventRepository, times(1)).existsById(nonExistentId);
        verify(eventRepository, never()).deleteById(anyString()); // deleteById не має бути викликаний
    }


    // Тест, що перевіряє взаємодію з однією із замоканих залежностей.
    @Test
    void getAllEvents_shouldCallFindAllOnRepository() {
        // Arrange
        List<Event> expectedEvents = Arrays.asList(validEvent, new Event());
        when(eventRepository.findAll()).thenReturn(expectedEvents);

        // Act
        List<Event> result = eventService.getAllEvents();

        // Assert
        assertThat(result).isEqualTo(expectedEvents);
        // Перевіряємо, що метод findAll на eventRepository був викликаний рівно один раз
        verify(eventRepository, times(1)).findAll();
        verifyNoMoreInteractions(eventRepository); // Перевіряємо, що інших взаємодій не було
    }

    @Test
    void getEventById_shouldCallFindByIdOnRepository() {
        // Arrange
        String eventId = validEvent.getId();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(validEvent));

        // Act
        eventService.getEventById(eventId);

        // Assert
        verify(eventRepository, times(1)).findById(eventId);
    }

    // Тести для updateEvent (успішний сценарій та взаємодія)
    @Test
    void updateEvent_shouldReturnUpdatedEvent_whenFound() {
        // Arrange
        String eventId = validEvent.getId();
        Event updatedDetails = new Event();
        updatedDetails.setName("Updated Festival");
        updatedDetails.setDescription("Updated fun");
        updatedDetails.setLocation("New Park");
        updatedDetails.setEventType("Conference");
        updatedDetails.setDate(Date.from(LocalDateTime.now().plusDays(20).atZone(ZoneId.systemDefault()).toInstant()));
        updatedDetails.setOrganizer("New Organizer");

        // Мокуємо, що подія існує
        when(eventRepository.existsById(eventId)).thenReturn(true);
        // Мокуємо, що save повертає оновлену подію
        when(eventRepository.save(any(Event.class))).thenReturn(updatedDetails);

        // Act
        Event result = eventService.updateEvent(eventId, updatedDetails);

        // Assert
        assertNotNull(result);
        assertEquals(updatedDetails.getName(), result.getName());
        assertEquals(eventId, result.getId()); // ID має залишитися тим самим
        verify(eventRepository, times(1)).existsById(eventId);
        verify(eventRepository, times(1)).save(updatedDetails); // Перевіряємо, що save був викликаний з оновленим об'єктом
    }

    // Тести для deleteEvent (успішний сценарій)
    @Test
    void deleteEvent_shouldReturnTrue_whenDeleted() {
        // Arrange
        String eventId = validEvent.getId();
        when(eventRepository.existsById(eventId)).thenReturn(true);

        // Act
        boolean result = eventService.deleteEvent(eventId);

        // Assert
        assertTrue(result);
        verify(eventRepository, times(1)).existsById(eventId);
        verify(eventRepository, times(1)).deleteById(eventId);
    }

    // Тести для getTicketPrice (різні типи подій)
    @Test
    void getTicketPrice_shouldReturnConcertPrice_forConcertEvent() {
        // Arrange - concertEvent вже налаштований
        // Act
        double price = eventService.getTicketPrice(concertEvent);
        // Assert
        assertEquals(50.0 * 1.2, price);
    }

    @Test
    void getTicketPrice_shouldReturnTheaterPrice_forTheaterEvent() {
        // Arrange - theaterEvent вже налаштований
        // Act
        double price = eventService.getTicketPrice(theaterEvent);
        // Assert
        assertEquals(50.0 * 1.1, price);
    }

    @Test
    void getTicketPrice_shouldReturnBasePrice_forOtherEventTypes() {
        // Arrange - otherEvent вже налаштований
        // Act
        double price = eventService.getTicketPrice(otherEvent);
        // Assert
        assertEquals(50.0, price);
    }

    // Тести для getEventsByFilter (виправлені)
    @Test
    void getEventsByFilter_shouldReturnEventsByName() {
        // Arrange
        List<Event> eventsFoundByName = Collections.singletonList(validEvent);
        when(eventRepository.findByName("Summer Festival")).thenReturn(eventsFoundByName);

        // Act
        List<Event> result = eventService.getEventsByFilter("Summer Festival", null, null);

        // Assert
        assertThat(result).isEqualTo(eventsFoundByName);
        verify(eventRepository, times(1)).findByName("Summer Festival");
        // Жоден інший метод не має бути викликаний, якщо findByName спрацював першим
        verify(eventRepository, never()).findAll();
        verify(eventRepository, never()).findByEventType(anyString());
    }

    @Test
    void getEventsByFilter_shouldReturnEventsByLocation() {
        // Arrange
        Event event1 = new Event();
        event1.setName("Event1");
        event1.setLocation("Kyiv Expo Center");
        event1.setEventType("Conference");

        Event event2 = new Event();
        event2.setName("Event2");
        event2.setLocation("Lviv Arena");
        event2.setEventType("Sport");

        List<Event> allEvents = Arrays.asList(event1, event2);

        // **Важливо:** findByName та findByEventType не викликаються взагалі,
        // якщо їх параметри (name, eventType) є null/порожніми в сервісі.
        // Тому їх не потрібно мокувати (when) і не потрібно перевіряти, що вони були викликані (verify).
        // Достатньо лише мокувати findAll, оскільки він викликається для фільтрації по локації.
        when(eventRepository.findAll()).thenReturn(allEvents); // findAll має бути викликаний для фільтрації по location

        // Act
        List<Event> result = eventService.getEventsByFilter(null, "kyiv", null);

        // Assert
        assertThat(result).containsExactly(event1);
        // Перевіряємо, що findByName та findByEventType ніколи не були викликані
        verify(eventRepository, never()).findByName(anyString());
        verify(eventRepository, never()).findByEventType(anyString());
        verify(eventRepository, times(1)).findAll(); // findAll має бути викликаний для фільтрації
    }


    @Test
    void getEventsByFilter_shouldReturnEventsByEventType() {
        // Arrange
        List<Event> eventsFoundByType = Collections.singletonList(validEvent);
        // **Важливо:** findByName не викликається взагалі, якщо його параметр (name) є null/порожнім.
        // Достатньо лише мокувати findByEventType.
        when(eventRepository.findByEventType("Concert")).thenReturn(eventsFoundByType);
        // findAll не має бути викликаний, якщо findByEventType спрацював

        // Act
        List<Event> result = eventService.getEventsByFilter(null, null, "Concert");

        // Assert
        assertThat(result).isEqualTo(eventsFoundByType);
        // Перевіряємо, що findByName ніколи не був викликаний, а findByEventType був викликаний з "Concert"
        verify(eventRepository, never()).findByName(anyString());
        verify(eventRepository, times(1)).findByEventType("Concert");
        verify(eventRepository, never()).findAll(); // findAll не має бути викликаний
    }


    @Test
    void getEventsByFilter_shouldReturnAllEvents_whenNoFiltersProvided() {
        // Arrange
        List<Event> allEvents = Arrays.asList(validEvent, concertEvent, theaterEvent);
        when(eventRepository.findAll()).thenReturn(allEvents);

        // Act
        List<Event> result = eventService.getEventsByFilter(null, null, null);

        // Assert
        assertThat(result).isEqualTo(allEvents);
        verify(eventRepository, times(1)).findAll();
        verify(eventRepository, never()).findByName(anyString());
        verify(eventRepository, never()).findByEventType(anyString());
    }
}