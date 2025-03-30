package one.expressdev.geekmer_hub.frontend;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private TokenStore tokenStore;

    public CustomAuthenticationProvider(TokenStore tokenStore) {
        super();
        this.tokenStore = tokenStore;
    }

    @Override
    public Authentication authenticate(final Authentication authentication)
        throws AuthenticationException {
        System.out.println("Llegué Custom Authentication Provider: ");
        System.out.println("Authentication: " + authentication);
        final String name = authentication.getName();
        System.out.println("Name: " + name);
        final String password = authentication.getCredentials().toString();
        System.out.println("Password: " + password);

        System.out.println("Custom Authentication Provider: " + name);
        //log.info("Login Success");

        final MultiValueMap requestBody = new LinkedMultiValueMap<>();
        requestBody.add("user", name);
        requestBody.add("password", password);

        System.out.println("Request Body: " + requestBody);

        final var restTemplate = new RestTemplate();
        final var responseEntity = restTemplate.postForEntity(
            "http://localhost:8080/login",
            requestBody,
            String.class
        );

        System.out.println("Response Entity: " + responseEntity);

        tokenStore.setToken(responseEntity.getBody());

        System.out.println("Token Store: " + tokenStore.getToken());

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new BadCredentialsException("Invalid username or password");
        }

        List authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        Authentication authenticatedToken =
            new UsernamePasswordAuthenticationToken(
                name,
                password,
                authorities
            );

        return authenticatedToken;
    }

    @Override
    public boolean supports(Class authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
