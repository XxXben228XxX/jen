package com.example.demo;

import com.example.demo.config.repository.TicketRepository;
import com.example.demo.config.service.TicketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; // Цей імпорт більше не потрібен, якщо ви його не використовуєте

import org.springframework.context.ConfigurableApplicationContext;
import java.time.format.DateTimeFormatter;


// Видалили 'exclude = {SecurityAutoConfiguration.class}'
@SpringBootApplication
public class Demo6Application {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Demo6Application.class, args);

		TicketService ticketService = context.getBean(TicketService.class);
		double price = ticketService.calculateTicketPrice("Концерт", 25);
		System.out.println("Ціна квитка: " + price);

		TicketRepository ticketRepository = context.getBean(TicketRepository.class);
		ticketRepository.save("Квиток на концерт для особи 25 років");
		System.out.println("Усі збережені квитки: " + ticketRepository.getAll());

		DateTimeFormatter dateFormatter = context.getBean(DateTimeFormatter.class);
		System.out.println("Поточна дата: " + dateFormatter.format(java.time.LocalDateTime.now()));
	}
}