package one.expressdev.geekmer_hub.frontend.security;

import one.expressdev.geekmer_hub.frontend.TokenStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;  
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;  
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;  
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;  
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;  
import java.util.List;  

 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomAuthenticationProvider.
 * Assumes RestTemplate and PasswordEncoder are injected.
 */
@ExtendWith(MockitoExtension.class)  
class CustomAuthenticationProviderTest {

    @Mock  
    private TokenStore tokenStore;

    @Mock  
    private RestTemplate restTemplate;

    @Mock  
    private PasswordEncoder passwordEncoder;  

    @InjectMocks  
    private CustomAuthenticationProvider customAuthenticationProvider;


    private final String backendLoginUrl = "http://backend:8080/login";
    private final String testUser = "testuser";
    private final String testPassword = "password123";
    private final String testToken = "backend-jwt-token";

    @BeforeEach
    void setUp() {
         
    }

     

    @Test
    @DisplayName("supports() should return true for UsernamePasswordAuthenticationToken")
    void supports_UsernamePasswordAuthenticationToken_shouldReturnTrue() {
        assertTrue(customAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("supports() should return false for other Authentication types")
    void supports_otherAuthenticationType_shouldReturnFalse() {
        assertFalse(customAuthenticationProvider.supports(TestingAuthenticationToken.class));  
    }

     

    @Test
    @DisplayName("authenticate() success should call backend, store token, and return authenticated token")
    void authenticate_success_shouldReturnAuthenticatedToken() {
         
        UsernamePasswordAuthenticationToken inputAuth =
            new UsernamePasswordAuthenticationToken(testUser, testPassword);
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(testToken, HttpStatus.OK);
        when(restTemplate.postForEntity(
                eq(backendLoginUrl), any(MultiValueMap.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

         
        Authentication resultAuth = customAuthenticationProvider.authenticate(inputAuth);

         
        ArgumentCaptor<MultiValueMap<String, String>> bodyCaptor = ArgumentCaptor.forClass(MultiValueMap.class);
        verify(restTemplate).postForEntity(eq(backendLoginUrl), bodyCaptor.capture(), eq(String.class));
        assertEquals(testUser, bodyCaptor.getValue().getFirst("user"));
        assertEquals(testPassword, bodyCaptor.getValue().getFirst("password"));
        verify(tokenStore).setToken(testToken);
        assertNotNull(resultAuth);
        assertTrue(resultAuth.isAuthenticated());
        assertEquals(testUser, resultAuth.getName());

         
         
        assertNull(resultAuth.getCredentials(), "Credentials should be erased (null) after successful authentication");

        assertNotNull(resultAuth.getAuthorities());
        assertEquals(1, resultAuth.getAuthorities().size(), "Should have exactly one authority");
        assertTrue(resultAuth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")), "Should contain ROLE_USER authority");
    }

    @Test
    @DisplayName("authenticate() when backend returns non-OK status should throw BadCredentialsException")
    void authenticate_backendReturnsNonOk_shouldThrowBadCredentials() {
         
        UsernamePasswordAuthenticationToken inputAuth =
            new UsernamePasswordAuthenticationToken(testUser, testPassword);
         
        HttpStatus nonOkStatus = HttpStatus.FORBIDDEN;
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("Access Denied", nonOkStatus);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(mockResponseEntity);

         
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            customAuthenticationProvider.authenticate(inputAuth);
        });

         
         
        assertEquals("Invalid username or password (backend status: " + nonOkStatus + ")", exception.getMessage());
        verify(tokenStore, never()).setToken(anyString());
    }

     @Test
     @DisplayName("authenticate() when backend call throws HttpClientErrorException should throw BadCredentialsException")
     void authenticate_backendThrowsHttpClientError_shouldThrowBadCredentials() {
          
         UsernamePasswordAuthenticationToken inputAuth =
             new UsernamePasswordAuthenticationToken(testUser, testPassword);
         HttpStatus errorStatus = HttpStatus.UNAUTHORIZED;
         when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                 .thenThrow(new HttpClientErrorException(errorStatus, "Unauthorized"));

          
         BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
             customAuthenticationProvider.authenticate(inputAuth);
         }, "Expected BadCredentialsException when backend throws HttpClientErrorException");

          
          
         assertEquals("Invalid username or password (backend error: " + errorStatus + ")", exception.getMessage());
         verify(tokenStore, never()).setToken(anyString());
     }


    @Test
    @DisplayName("authenticate() when backend call throws RestClientException should throw BadCredentialsException")
    void authenticate_backendThrowsRestClientException_shouldThrowBadCredentials() {
         
        UsernamePasswordAuthenticationToken inputAuth =
            new UsernamePasswordAuthenticationToken(testUser, testPassword);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

         
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            customAuthenticationProvider.authenticate(inputAuth);
        });

         
        assertEquals("Unable to contact authentication service.", exception.getMessage());
        verify(tokenStore, never()).setToken(anyString());
    }

    @Test
    @DisplayName("authenticate() when backend returns OK but empty token should throw BadCredentialsException")
    void authenticate_backendReturnsOkEmptyToken_shouldThrowBadCredentials() {
         
        UsernamePasswordAuthenticationToken inputAuth =
            new UsernamePasswordAuthenticationToken(testUser, testPassword);
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(null, HttpStatus.OK);  
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(mockResponseEntity);

         
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            customAuthenticationProvider.authenticate(inputAuth);
        });

         
        assertEquals("Authentication service returned an invalid token.", exception.getMessage());
        verify(tokenStore, never()).setToken(anyString());
    }

     @Test
     @DisplayName("authenticate() when unexpected internal exception occurs should throw AuthenticationServiceException")
     void authenticate_unexpectedException_shouldThrowAuthenticationServiceException() {
          
         UsernamePasswordAuthenticationToken inputAuth =
             new UsernamePasswordAuthenticationToken(testUser, testPassword);
         RuntimeException internalError = new RuntimeException("Something else broke");
         when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                 .thenThrow(internalError);  

          
         AuthenticationServiceException exception = assertThrows(AuthenticationServiceException.class, () -> {
             customAuthenticationProvider.authenticate(inputAuth);
         });

          
         assertEquals("Unexpected error during authentication.", exception.getMessage());
         assertSame(internalError, exception.getCause());  
         verify(tokenStore, never()).setToken(anyString());
     }


     
    private static class TestingAuthenticationToken implements Authentication {
        @Override public Collection<? extends GrantedAuthority> getAuthorities() { return null; }
        @Override public Object getCredentials() { return null; }
        @Override public Object getDetails() { return null; }
        @Override public Object getPrincipal() { return null; }
        @Override public boolean isAuthenticated() { return false; }
        @Override public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException { }
        @Override public String getName() { return null; }
    }
}
