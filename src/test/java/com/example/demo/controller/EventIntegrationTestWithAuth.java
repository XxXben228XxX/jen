package com.example.demo.controller;

import com.example.demo.model.ERole;
import com.example.demo.model.Event;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.dto.EventDto;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtResponse;
import com.example.demo.security.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional; // Keep this for findById assertions
import java.util.Set;
import java.util.List; // Import List for findByName

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class EventIntegrationTestWithAuth {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventRepository eventRepository;

    private String adminUsername = "admin_test";
    private String adminPassword = "password123";
    private String adminEmail = "admin_test@example.com";

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN.name())
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ADMIN.name())));
        Role userRole = roleRepository.findByName(ERole.ROLE_USER.name())
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER.name())));

        User adminUser = new User(adminUsername, adminEmail, passwordEncoder.encode(adminPassword));
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        adminUser.setRoles(roles);
        userRepository.save(adminUser);
    }

    private String obtainJwtToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        String jsonLoginRequest = objectMapper.writeValueAsString(loginRequest);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLoginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseString, JwtResponse.class);
        return jwtResponse.getAccessToken();
    }

    @Test
    void createEvent_shouldReturnCreatedEventAndSaveToDb_whenAdminAuthenticated() throws Exception {
        String adminToken = obtainJwtToken(adminUsername, adminPassword);

        EventDto eventDto = new EventDto();
        eventDto.setName("New Event Title");
        eventDto.setDescription("This is a test event.");
        eventDto.setLocation("Test Venue");
        eventDto.setEventType("Concert");
        eventDto.setOrganizer("Test Organizer Inc.");
        eventDto.setDate(Date.from(LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()));

        String jsonEventDto = objectMapper.writeValueAsString(eventDto);

        MvcResult result = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEventDto))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(eventDto.getName()))
                .andExpect(jsonPath("$.description").value(eventDto.getDescription()))
                .andExpect(jsonPath("$.location").value(eventDto.getLocation()))
                .andExpect(jsonPath("$.eventType").value(eventDto.getEventType()))
                .andExpect(jsonPath("$.organizer").value(eventDto.getOrganizer()))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Event createdEventFromResponse = objectMapper.readValue(responseContent, Event.class);

        assertThat(createdEventFromResponse.getId()).isNotNull();
        Optional<Event> eventInDb = eventRepository.findById(createdEventFromResponse.getId());

        assertThat(eventInDb).isPresent();
        assertThat(eventInDb.get().getName()).isEqualTo(eventDto.getName());
        assertThat(eventInDb.get().getDescription()).isEqualTo(eventDto.getDescription());
        assertThat(eventInDb.get().getLocation()).isEqualTo(eventDto.getLocation());
        assertThat(eventInDb.get().getEventType()).isEqualTo(eventDto.getEventType());
        assertThat(eventInDb.get().getOrganizer()).isEqualTo(eventDto.getOrganizer());
        assertThat(eventInDb.get().getEventDate().withNano(0))
                .isEqualTo(eventDto.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().withNano(0));
    }

    @Test
    void createEvent_shouldReturnForbidden_whenUserAuthenticated() throws Exception {
        String userUsername = "user_test";
        String userPassword = "userpass";
        String userEmail = "user_test@example.com";

        Role userRole = roleRepository.findByName(ERole.ROLE_USER.name())
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER.name())));

        User regularUser = new User(userUsername, userEmail, passwordEncoder.encode(userPassword));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        userRepository.save(regularUser);

        String userToken = obtainJwtToken(userUsername, userPassword);

        EventDto eventDto = new EventDto();
        eventDto.setName("Another Event");
        eventDto.setDescription("Description");
        eventDto.setLocation("Location");
        eventDto.setEventType("Type");
        eventDto.setOrganizer("Some Organizer");
        eventDto.setDate(new Date());

        String jsonEventDto = objectMapper.writeValueAsString(eventDto);

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEventDto))
                .andExpect(status().isForbidden());

        // FIX: Change to .isEmpty() because findByName likely returns List<Event>
        List<Event> events = eventRepository.findByName(eventDto.getName());
        assertThat(events).isEmpty(); // Line 198
    }

    @Test
    void createEvent_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        EventDto eventDto = new EventDto();
        eventDto.setName("Unauthorized Event");
        eventDto.setDescription("Description");
        eventDto.setLocation("Location");
        eventDto.setEventType("Type");
        eventDto.setOrganizer("No Auth Organizer");
        eventDto.setDate(new Date());

        String jsonEventDto = objectMapper.writeValueAsString(eventDto);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEventDto))
                .andExpect(status().isUnauthorized());

        // FIX: Change to .isEmpty() because findByName likely returns List<Event>
        List<Event> events = eventRepository.findByName(eventDto.getName());
        assertThat(events).isEmpty(); // Line 221
    }
}