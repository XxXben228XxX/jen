package com.example.demo.repository;

import com.example.demo.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    // Тут ви можете додавати спеціальні методи для Venue, якщо вони знадобляться.
    // Наприклад, пошук за назвою:
     List<Venue> findByName(String name);
}