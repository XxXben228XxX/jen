package com.example.demo.security;

import com.example.demo.security.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders; // Додаємо цей імпорт!
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private String testJwtSecretBase64;
    private Key testSigningKey;

    private final int TEST_JWT_EXPIRATION_MS = 3600000;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testSigningKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        // --- ВИПРАВЛЕНО ТУТ ---
        testJwtSecretBase64 = Encoders.BASE64.encode(testSigningKey.getEncoded()); // Використовуємо Encoders!
        // ----------------------

        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", testJwtSecretBase64);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", TEST_JWT_EXPIRATION_MS);

        Mockito.when(userDetails.getUsername()).thenReturn("testuser");
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        Mockito.doReturn(authorities).when(userDetails).getAuthorities();
    }

    private Key getSigningKey(String secretBase64) {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    @DisplayName("should generate valid JWT token with correct claims")
    void generateJwtToken_success() {
        String token = jwtUtils.generateJwtToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(testSigningKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testuser", claims.getSubject());
        assertTrue(claims.containsKey("roles"));
        assertEquals(Collections.singletonList("ROLE_USER"), claims.get("roles"));

        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(expiration.getTime() - issuedAt.getTime() >= TEST_JWT_EXPIRATION_MS - 1000);
    }

    @Test
    @DisplayName("should validate a valid JWT token")
    void validateJwtToken_validToken() {
        String validToken = jwtUtils.generateJwtToken(userDetails);
        boolean isValid = jwtUtils.validateJwtToken(validToken);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("should invalidate an expired JWT token")
    void validateJwtToken_expiredToken() throws InterruptedException {
        int veryShortExpirationMs = 1;
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", veryShortExpirationMs);
        String expiredToken = jwtUtils.generateJwtToken(userDetails);

        Thread.sleep(veryShortExpirationMs + 100);

        boolean isValid = jwtUtils.validateJwtToken(expiredToken);
        assertFalse(isValid);

        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", TEST_JWT_EXPIRATION_MS);
    }

    @Test
    @DisplayName("should invalidate JWT token with wrong signature")
    void validateJwtToken_wrongSignature() {
        Key wrongKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String tokenWithWrongSignature = Jwts.builder()
                .setSubject("someuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + TEST_JWT_EXPIRATION_MS))
                .signWith(wrongKey, SignatureAlgorithm.HS512)
                .compact();

        boolean isValid = jwtUtils.validateJwtToken(tokenWithWrongSignature);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("should invalidate JWT token with invalid format")
    void validateJwtToken_invalidFormat() {
        String invalidToken = "this.is.not.a.valid.jwt";
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("should invalidate null JWT token")
    void validateJwtToken_nullToken() {
        String nullToken = null;
        boolean isValid = jwtUtils.validateJwtToken(nullToken);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("should extract username from valid JWT token")
    void getUserNameFromJwtToken_success() {
        String token = jwtUtils.generateJwtToken(userDetails);
        String username = jwtUtils.getUserNameFromJwtToken(token);
        assertNotNull(username);
        assertEquals("testuser", username);
    }
}