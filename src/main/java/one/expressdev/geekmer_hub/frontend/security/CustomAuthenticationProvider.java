package one.expressdev.geekmer_hub.frontend.security;

import java.util.ArrayList;
import java.util.List;
import one.expressdev.geekmer_hub.frontend.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

  private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

  private final TokenStore tokenStore;
  private final RestTemplate restTemplate;
  private final PasswordEncoder passwordEncoder;

  public CustomAuthenticationProvider(
      TokenStore tokenStore, RestTemplate restTemplate, PasswordEncoder passwordEncoder) {
    log.info("Initializing CustomAuthenticationProvider with injected dependencies.");
    this.tokenStore = tokenStore;
    this.restTemplate = restTemplate;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Authentication authenticate(final Authentication authentication)
      throws AuthenticationException {
    log.debug("Attempting authentication for user: {}", authentication.getName());
    final String name = authentication.getName();
    final String password = authentication.getCredentials().toString();

    final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("user", name);
    requestBody.add("password", password);

    log.debug("Sending login request to backend for user: {}", name);
    ResponseEntity<String> responseEntity;
    try {

      responseEntity =
          restTemplate.postForEntity("http://backend:8080/login", requestBody, String.class);
      log.debug("Received response from backend: {}", responseEntity.getStatusCode());

    } catch (HttpClientErrorException e) {

      log.error(
          "HTTP error during backend login for user {}: {} {}",
          name,
          e.getStatusCode(),
          e.getStatusText(),
          e);

      throw new BadCredentialsException(
          "Invalid username or password (backend error: " + e.getStatusCode() + ")");
    } catch (RestClientException e) {

      log.error(
          "Communication error during backend login for user {}: {}", name, e.getMessage(), e);
      throw new BadCredentialsException("Unable to contact authentication service.", e);
    } catch (Exception e) {

      log.error("Unexpected error during backend login for user {}: {}", name, e.getMessage(), e);
      throw new AuthenticationServiceException("Unexpected error during authentication.", e);
    }

    if (responseEntity.getStatusCode() != HttpStatus.OK) {
      log.warn(
          "Backend login failed for user {} with status: {}", name, responseEntity.getStatusCode());
      throw new BadCredentialsException(
          "Invalid username or password (backend status: " + responseEntity.getStatusCode() + ")");
    }

    String receivedToken = responseEntity.getBody();
    if (receivedToken == null || receivedToken.isBlank()) {
      log.error("Backend login returned OK but with null or empty token for user {}", name);
      throw new BadCredentialsException("Authentication service returned an invalid token.");
    }

    log.info("Authentication successful for user: {}", name);
    tokenStore.setToken(receivedToken);
    log.debug("Token stored successfully for user: {}", name);

    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

    return new UsernamePasswordAuthenticationToken(name, null, authorities);
  }

  @Override
  public boolean supports(Class<?> authentication) {

    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
