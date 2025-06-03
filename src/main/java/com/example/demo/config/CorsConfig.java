//package com.example.demo.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration // Ця анотація позначає клас як джерело визначень бінів конфігурації
//public class CorsConfig implements WebMvcConfigurer { // Реалізуємо WebMvcConfigurer для налаштування MVC
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**") // Змінено з "/api/**" на "/**"
//                // Це дозволить CORS для ВСІХ шляхів у твоєму додатку,
//                // включаючи /api/auth/signup та /api/auth/signin,
//                // які можуть бути не під /api/events, а просто під /api/.
//                // Це є безпечнішим, ніж мати дві конфігурації.
//                .allowedOrigins("http://localhost:3000") // Дозволяє запити лише з твого React-додатка
//                // Завжди вказуй конкретні домени замість "*" у production!
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Додано "OPTIONS"
//                // OPTIONS запити важливі для CORS-перевірок (preflight requests)
//                .allowedHeaders("*") // Дозволяє всі заголовки (включаючи Authorization для JWT)
//                .allowCredentials(true); // Дозволяє передачу облікових даних (наприклад, JWT-токенів у заголовку Authorization)
//    }
//}