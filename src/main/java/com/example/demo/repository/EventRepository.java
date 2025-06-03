package com.example.demo.repository;

import com.example.demo.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    List<Event> findByName(String name);

    List<Event> findByVenueId(Long venueId);

    List<Event> findByEventDateAfter(LocalDateTime eventDate);

    List<Event> findByVenue_NameOrderByEventDateDesc(String venueName);

    // Додаткові похідні запити (за бажанням)
    List<Event> findByEventType(String eventType);

    List<Event> findByLocationContainingIgnoreCase(String keyword);
}