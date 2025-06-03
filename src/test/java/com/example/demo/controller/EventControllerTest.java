package com.example.demo.controller;

import com.example.demo.dto.EventDto;
import com.example.demo.model.Event;
import com.example.demo.model.Venue;
import com.example.demo.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Додано
import org.springframework.boot.test.context.SpringBootTest; // Змінено з WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc // Потрібно для ін'єкції MockMvc в @SpringBootTest
@Transactional
@ActiveProfiles("test")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // Залишимо, припускаючи, що ти хочеш мокувати сервіс
    private EventService eventService;

    private Event event1;
    private Event event2;
    private EventDto eventDto;

    @BeforeEach
    void setUp() {
        event1 = new Event();
        event1.setId("1");
        event1.setName("Concert");
        event1.setDescription("Live music show");
        event1.setLocation("City Hall");
        event1.setOrganizer("Music Events Inc.");
        event1.setEventType("Music");
        event1.setDate(new Date());

        event2 = new Event();
        event2.setId("2");
        event2.setName("Art Exhibition");
        event2.setDescription("Local artists display");
        event2.setLocation("Gallery X");
        event2.setOrganizer("Art Lovers Society");
        event2.setEventType("Art");
        event2.setDate(new Date());

        eventDto = new EventDto();
        eventDto.setName("New Event");
        eventDto.setDescription("New description");
        eventDto.setLocation("New Location");
        eventDto.setEventType("New Type");
        // eventDto.setOrganizer("New Organizer"); // Якщо EventDto також має organizer
    }

    // Всі @Test методи залишаються без змін, оскільки вони використовують @WithMockUser
    // та MockMvc, що працює з @SpringBootTest + @AutoConfigureMockMvc.
    // ... (весь інший код тестів) ...

    @Test
    @WithMockUser(roles = {"USER"})
    void getEvents_shouldReturnAllEvents() throws Exception {
        when(eventService.getAllEvents()).thenReturn(Arrays.asList(event1, event2));

        mockMvc.perform(get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(event1.getName()))
                .andExpect(jsonPath("$[1].name").value(event2.getName()));

        verify(eventService, times(1)).getAllEvents();
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getEventById_shouldReturnEvent_whenFound() throws Exception {
        when(eventService.getEventById("1")).thenReturn(event1);

        mockMvc.perform(get("/api/events/{id}", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(event1.getName()));

        verify(eventService, times(1)).getEventById("1");
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getEventById_shouldReturnNotFound_whenNotFound() throws Exception {
        when(eventService.getEventById("99")).thenReturn(null);

        mockMvc.perform(get("/api/events/{id}", "99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(eventService, times(1)).getEventById("99");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void addEvent_shouldReturnCreatedEvent() throws Exception {
        Event newEvent = new Event();
        newEvent.setId("3");
        newEvent.setName(eventDto.getName());
        newEvent.setDescription(eventDto.getDescription());
        newEvent.setLocation(eventDto.getLocation());
        newEvent.setEventType(eventDto.getEventType());
        newEvent.setDate(new Date());
        // newEvent.setOrganizer(eventDto.getOrganizer()); // Розкоментуй, якщо EventDto має organizer

        when(eventService.addEvent(any(Event.class))).thenReturn(newEvent);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(newEvent.getName()));

        verify(eventService, times(1)).addEvent(any(Event.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void addEvent_shouldReturnForbiddenForUser() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateEvent_shouldReturnUpdatedEvent_whenFound() throws Exception {
        Event updatedEvent = new Event();
        updatedEvent.setId("1");
        updatedEvent.setName("Updated Name");
        updatedEvent.setDescription("Updated Desc");
        updatedEvent.setLocation("Updated Loc");
        updatedEvent.setEventType("Updated Type");
        updatedEvent.setDate(new Date());
        // updatedEvent.setOrganizer("Updated Organizer"); // Розкоментуй, якщо EventDto має organizer

        when(eventService.updateEvent(eq("1"), any(Event.class))).thenReturn(updatedEvent);

        mockMvc.perform(put("/api/events/{id}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedEvent.getName()));

        verify(eventService, times(1)).updateEvent(eq("1"), any(Event.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateEvent_shouldReturnNotFound_whenNotFound() throws Exception {
        when(eventService.updateEvent(eq("99"), any(Event.class))).thenReturn(null);

        mockMvc.perform(put("/api/events/{id}", "99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isNotFound());

        verify(eventService, times(1)).updateEvent(eq("99"), any(Event.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void updateEvent_shouldReturnForbiddenForUser() throws Exception {
        mockMvc.perform(put("/api/events/{id}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteEvent_shouldReturnNoContent_whenDeleted() throws Exception {
        when(eventService.deleteEvent("1")).thenReturn(true);

        mockMvc.perform(delete("/api/events/{id}", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(eventService, times(1)).deleteEvent("1");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteEvent_shouldReturnNotFound_whenNotDeleted() throws Exception {
        when(eventService.deleteEvent("99")).thenReturn(false);

        mockMvc.perform(delete("/api/events/{id}", "99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(eventService, times(1)).deleteEvent("99");
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void deleteEvent_shouldReturnForbiddenForUser() throws Exception {
        mockMvc.perform(delete("/api/events/{id}", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getEventPrice_shouldReturnPrice_whenFound() throws Exception {
        when(eventService.getEventById("1")).thenReturn(event1);
        when(eventService.getTicketPrice(event1)).thenReturn(100.0);

        mockMvc.perform(get("/api/events/{id}/price", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(100.0));

        verify(eventService, times(1)).getEventById("1");
        verify(eventService, times(1)).getTicketPrice(event1);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getEventPrice_shouldReturnNotFound_whenEventNotFound() throws Exception {
        when(eventService.getEventById("99")).thenReturn(null);

        mockMvc.perform(get("/api/events/{id}/price", "99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(eventService, times(1)).getEventById("99");
        verify(eventService, never()).getTicketPrice(any(Event.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getEventsByFilter_shouldReturnFilteredEvents() throws Exception {
        when(eventService.getEventsByFilter(eq("Concert"), eq("City Hall"), eq("Music")))
                .thenReturn(Collections.singletonList(event1));

        mockMvc.perform(get("/api/events/filter")
                        .param("name", "Concert")
                        .param("location", "City Hall")
                        .param("eventType", "Music")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(event1.getName()));

        verify(eventService, times(1)).getEventsByFilter(eq("Concert"), eq("City Hall"), eq("Music"));
    }

    @Test
    void getEvents_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}