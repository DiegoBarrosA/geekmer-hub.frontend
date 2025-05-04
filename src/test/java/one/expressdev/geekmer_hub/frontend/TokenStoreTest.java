package one.expressdev.geekmer_hub.frontend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TokenStore component.
 */
class TokenStoreTest {

    private TokenStore tokenStore;

    /**
     * Set up a new TokenStore instance before each test method.
     */
    @BeforeEach
    void setUp() {
        tokenStore = new TokenStore();
    }

    /**
     * Tests that setting a token value using setToken()
     * allows retrieving the same value using getToken().
     */
    @Test
    @DisplayName("Should return the set token")
    void testSetAndGetToken() {
        // Arrange: Define a test token value
        String expectedToken = "test-jwt-token-12345";

        // Act: Set the token in the store
        tokenStore.setToken(expectedToken);

        // Assert: Retrieve the token and check if it matches the expected value
        String actualToken = tokenStore.getToken();
        assertEquals(expectedToken, actualToken, "The retrieved token should match the one that was set.");
    }

    /**
     * Tests that retrieving a token using getToken()
     * returns null if no token has been previously set.
     */
    @Test
    @DisplayName("Should return null when token is not set")
    void testGetToken_whenNotSet_shouldReturnNull() {
        // Act: Retrieve the token without setting it first
        String actualToken = tokenStore.getToken();

        // Assert: Check if the retrieved token is null
        assertNull(actualToken, "Token should be null initially if not set.");
    }

    /**
     * Tests that setting the token to null works correctly.
     */
    @Test
    @DisplayName("Should allow setting token to null")
    void testSetTokenToNull() {
        // Arrange: Set an initial token
        tokenStore.setToken("initial-token");

        // Act: Set the token to null
        tokenStore.setToken(null);

        // Assert: Retrieve the token and check if it's null
        String actualToken = tokenStore.getToken();
        assertNull(actualToken, "Token should be null after being explicitly set to null.");
    }

    /**
     * Tests that setting a new token overwrites the previous one.
     */
    @Test
    @DisplayName("Should overwrite existing token when set again")
    void testSetToken_overwritesExisting() {
        // Arrange: Set an initial token
        String initialToken = "first-token";
        tokenStore.setToken(initialToken);

        // Act: Set a new token value
        String newToken = "second-token-overwrite";
        tokenStore.setToken(newToken);

        // Assert: Retrieve the token and check if it matches the new value
        String actualToken = tokenStore.getToken();
        assertEquals(newToken, actualToken, "Setting a new token should overwrite the previous one.");
        assertNotEquals(initialToken, actualToken, "The initial token should no longer be present.");
    }
}
