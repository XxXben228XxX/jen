package com.example.demo.config.service;

import org.springframework.stereotype.Service;

@Service
public class TicketService {

    public double calculateTicketPrice(String eventType, int age) {
        double basePrice = 50.0;
        if ("Концерт".equals(eventType)) {
            return basePrice * 1.2;
        } else if ("Театр".equals(eventType)) {
            return basePrice * 1.1;
        } else {
            return basePrice;
        }
    }
}