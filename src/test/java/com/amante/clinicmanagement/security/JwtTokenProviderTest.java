package com.amante.clinicmanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // Must be at least 32 characters (256 bits) for HMAC-SHA algorithms
    private final String TEST_SECRET = "superSecretKeyForTestingTheClinicApp123!";
    private final long TEST_EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Manually inject the @Value fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", TEST_EXPIRATION);
    }

    @Test
    void testGenerateToken_And_GetEmailFromToken_Success() {
        // Arrange
        String email = "doctor@test.com";

        // Act
        String token = jwtTokenProvider.generateToken(email);
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(email, extractedEmail);
    }

    @Test
    void testValidateToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken("user@test.com");

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidSignature_ReturnsFalse() {
        // Create a different provider with a different secret to generate a token with wrong signature
        JwtTokenProvider differentProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(differentProvider, "jwtSecret", "differentSecretKeyForTestingPurposes123!");
        ReflectionTestUtils.setField(differentProvider, "jwtExpiration", TEST_EXPIRATION);

        // Token signed with different secret
        String tokenWithDifferentSignature = differentProvider.generateToken("user@test.com");

        // Act - Validate with original provider (different secret)
        boolean isValid = jwtTokenProvider.validateToken(tokenWithDifferentSignature);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Arrange
        // Create a new provider specifically to generate an expired token
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "jwtSecret", TEST_SECRET);
        // Set expiration to -1000ms (1 second ago)
        ReflectionTestUtils.setField(expiredProvider, "jwtExpiration", -1000L);

        String expiredToken = expiredProvider.generateToken("expired@test.com");

        // Act
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Assert
        assertFalse(isValid); // Hits the catch (JwtException -> ExpiredJwtException)
    }

    @Test
    void testValidateToken_MalformedToken_ReturnsFalse() {
        // Arrange
        String malformedToken = "not.a.valid.jwt";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Assert
        assertFalse(isValid); // Hits the catch (JwtException -> MalformedJwtException)
    }

    @Test
    void testValidateToken_NullOrEmpty_ReturnsFalse() {
        // Arrange
        String nullToken = null;

        // Act
        boolean isValid = jwtTokenProvider.validateToken(nullToken);

        // Assert
        assertFalse(isValid); // Hits the catch (IllegalArgumentException)
    }
}