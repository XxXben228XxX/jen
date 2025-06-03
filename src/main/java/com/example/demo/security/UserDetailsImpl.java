// src/main/java/com/example/demo/security/services/UserDetailsImpl.java
package com.example.demo.security; // Зверніть увагу на 'services' - це важлива частина шляху

import com.example.demo.model.User; // Імпорт вашої моделі User
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;

    @JsonIgnore // Щоб пароль не серіалізувався в JSON відповіді
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                // ВИПРАВЛЕНО: Видалено .name(), оскільки role.getName() вже повертає String
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    // --- Реалізація методів інтерфейсу UserDetails ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() { // Додаємо геттер для ID, він потрібен для JwtResponse
        return id;
    }

    public String getEmail() { // Додаємо геттер для Email, він потрібен для JwtResponse
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Зазвичай true, якщо немає логіки для закінчення терміну дії облікового запису
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Зазвичай true, якщо немає логіки для блокування облікового запису
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Зазвичай true, якщо немає логіки для закінчення терміну дії облікових даних
    }

    @Override
    public boolean isEnabled() {
        return true; // Зазвичай true, якщо немає логіки для вимкнення облікового запису
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}