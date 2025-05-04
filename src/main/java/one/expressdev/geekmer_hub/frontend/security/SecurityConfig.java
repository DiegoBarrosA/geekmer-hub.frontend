package one.expressdev.geekmer_hub.frontend.security;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer; // Import for lambda DSL defaults
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
@EnableWebSecurity(debug = true) // Keep debug=true only during development
public class SecurityConfig {

    @Autowired private CustomAuthenticationProvider customAuthenticationProvider;

    // This way of exposing AuthenticationManager might be needed if you use it elsewhere,
    // but often just registering the provider is enough.
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        String nonce = generateNonce(); // Nonce generated once per application start

        http
                // 1. Configure Headers (including CSP with Nonce)
                .headers(headers -> headers
                        .addHeaderWriter(new StaticHeadersWriter("X-Frame-Options", "DENY"))
                        .addHeaderWriter(new StaticHeadersWriter("X-XSS-Protection", "1; mode=block"))
                        .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"))
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; "
                                                + "img-src 'self' data:; " // Allow images from self and data URIs
                                                // Ensure the nonce is correctly added to inline style/script tags in Thymeleaf
                                                + "script-src 'self' 'nonce-" + nonce + "' https://cdn.example.com; "
                                                + "style-src 'self' 'nonce-" + nonce + "' https://cdn.example.com;"
                                )
                        )
                )
                // 2. Configure Authorization Rules (Order Matters!)
                .authorizeHttpRequests(authorize -> authorize
                        // --- Allow access to specific paths without authentication ---
                        .requestMatchers(
                                "/", // Allow access to the homepage?
                                "/style.css",
                                "/images/**", // Match image directory
                                "/js/**",     // Match JavaScript directory
                                "/webjars/**", // Common for frontend libraries
                                "/favicon.ico",
                                "/login",     // Allow access to login page
                                "/registro",  // Allow access to registration page
                                "/error"      // Allow access to default error page
                        ).permitAll() // Grant permission to everyone for the paths above

                        // --- Require authentication for ALL OTHER requests (Must be LAST) ---
                        .anyRequest().authenticated()
                )
                // 3. Configure Form Login
                .formLogin(Customizer.withDefaults()) // Use default login page and processing URL, ensure they are implicitly permitted or add .permitAll() if needed with custom pages
                // Or customize: .formLogin(formLogin -> formLogin.loginPage("/login").permitAll())

                // 4. Configure Logout
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // Redirect after logout
                        .permitAll() // Allow access to logout functionality
                );

        // Inject the AuthenticationManager bean configured above
        // This ensures your CustomAuthenticationProvider is used.
        http.authenticationManager(authenticationManager(http));

        return http.build();
    }

    // Helper method to generate a nonce
    private String generateNonce() {
        SecureRandom random = new SecureRandom();
        byte[] nonceBytes = new byte[16]; // Use a different variable name
        random.nextBytes(nonceBytes);
        return Base64.getEncoder().encodeToString(nonceBytes);
    }
}
