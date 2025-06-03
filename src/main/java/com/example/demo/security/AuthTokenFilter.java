// src/main/java/com/example/demo/security/jwt/AuthTokenFilter.java
package com.example.demo.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.security.UserDetailsServiceImpl; // Імпорт вашої UserDetailsServiceImpl

// Ця анотація @Component (або @Service) або біном у SecurityConfig
// дозволяють Spring управляти життєвим циклом фільтра.
// Ми додамо його як Bean в SecurityConfig пізніше.
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request); // Отримуємо JWT з запиту
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) { // Перевіряємо валідність токена
                String username = jwtUtils.getUserNameFromJwtToken(jwt); // Отримуємо ім'я користувача

                UserDetails userDetails = userDetailsService.loadUserByUsername(username); // Завантажуємо UserDetails
                // Створюємо об'єкт аутентифікації
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Встановлюємо об'єкт аутентифікації в SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response); // Продовжуємо виконання ланцюга фільтрів
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Видаляємо "Bearer " префікс
        }

        return null;
    }
}