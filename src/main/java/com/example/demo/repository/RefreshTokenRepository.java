package com.example.demo.repository;

import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Метод для пошуку refresh-токена за його рядковим значенням
    Optional<RefreshToken> findByToken(String token);

    // Метод для пошуку refresh-токена за користувачем
    // Зазвичай, для простоти, кожен користувач має один активний refresh-токен
    // Optional<RefreshToken> findByUser(User user); // Якщо OneToOne

    // Метод для видалення refresh-токена користувача. Корисно при логауті або оновленні
    @Modifying
    void deleteByUser(User user);

    Optional<RefreshToken> findByUser(User user); // Для OneToOne зв'язку

}