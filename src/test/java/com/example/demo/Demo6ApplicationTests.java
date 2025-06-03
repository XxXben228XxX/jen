package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // <-- ДОДАЙТЕ ЦЕЙ ІМПОРТ

@SpringBootTest
@ActiveProfiles("test")
class Demo6ApplicationTests {

	@Test
	void contextLoads() {
	}

}
