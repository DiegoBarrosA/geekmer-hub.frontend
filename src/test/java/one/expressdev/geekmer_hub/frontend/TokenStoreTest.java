package one.expressdev.geekmer_hub.frontend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class TokenStoreTest {

    private TokenStore tokenStore;

    
    @BeforeEach
    void setUp() {
        tokenStore = new TokenStore();
    }

    
    @Test
    @DisplayName("Should return the set token")
    void testSetAndGetToken() {
        
        String expectedToken = "test-jwt-token-12345";

        
        tokenStore.setToken(expectedToken);

        
        String actualToken = tokenStore.getToken();
        assertEquals(expectedToken, actualToken, "The retrieved token should match the one that was set.");
    }

    
    @Test
    @DisplayName("Should return null when token is not set")
    void testGetToken_whenNotSet_shouldReturnNull() {
        
        String actualToken = tokenStore.getToken();

        
        assertNull(actualToken, "Token should be null initially if not set.");
    }

    
    @Test
    @DisplayName("Should allow setting token to null")
    void testSetTokenToNull() {
        
        tokenStore.setToken("initial-token");

        
        tokenStore.setToken(null);

        
        String actualToken = tokenStore.getToken();
        assertNull(actualToken, "Token should be null after being explicitly set to null.");
    }

    
    @Test
    @DisplayName("Should overwrite existing token when set again")
    void testSetToken_overwritesExisting() {
        
        String initialToken = "first-token";
        tokenStore.setToken(initialToken);

        
        String newToken = "second-token-overwrite";
        tokenStore.setToken(newToken);

        
        String actualToken = tokenStore.getToken();
        assertEquals(newToken, actualToken, "Setting a new token should overwrite the previous one.");
        assertNotEquals(initialToken, actualToken, "The initial token should no longer be present.");
    }
}
