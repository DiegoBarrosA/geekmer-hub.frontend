package one.expressdev.geekmer_hub.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // Import Bean annotation
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Import BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder interface
import org.springframework.web.client.RestTemplate; // Import RestTemplate class

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Defines a bean for RestTemplate.
     * This allows Spring to inject RestTemplate wherever it's required,
     * such as in the HomeController.
     * @return A new RestTemplate instance.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Defines a bean for PasswordEncoder (using BCrypt).
     * This allows Spring to inject PasswordEncoder wherever it's required,
     * such as in the CustomAuthenticationProvider.
     * @return A BCryptPasswordEncoder instance.
     */
    @Bean // <-- Add this bean definition
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
