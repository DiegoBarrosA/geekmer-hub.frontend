package one.expressdev.geekmer_hub.frontend.security;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http)
        throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(
            customAuthenticationProvider
        );
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
        // Generate a nonce for the CSP
        String nonce = generateNonce();

        http
            .headers()
            .contentSecurityPolicy(
                "default-src 'self'; " + // Allow resources from the same origin
                "img-src 'self' data:; " + // Allow images from the same origin and data URIs
                "script-src 'self' 'nonce-" +
                nonce +
                "' https://cdn.example.com; " + // Allow scripts from self and a specific CDN
                "style-src 'self' 'nonce-" +
                nonce +
                "' https://cdn.example.com;" // Allow styles from self and a specific CDN
            )
            .and()
            .addHeaderWriter(new StaticHeadersWriter("X-Frame-Options", "DENY"))
            .addHeaderWriter(
                new StaticHeadersWriter("X-XSS-Protection", "1; mode=block")
            )
            .addHeaderWriter(
                new StaticHeadersWriter("X-Content-Type-Options", "nosniff")
            )
            .and()
            .authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .formLogin()
            .and()
            .logout();

        return http.build();
    }

    // Method to generate a nonce
    private String generateNonce() {
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        return Base64.getEncoder().encodeToString(nonce);
    }
}
