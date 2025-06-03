package com.example.demo.config.repository;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TicketRepository {

    private List<String> storedTickets = new ArrayList<>();

    public void save(String ticketInfo) {
        storedTickets.add(ticketInfo);
        System.out.println("Квиток збережено: " + ticketInfo);
    }

    public List<String> getAll() {
        return storedTickets;
    }
}
