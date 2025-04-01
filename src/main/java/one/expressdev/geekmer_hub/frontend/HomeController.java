package one.expressdev.geekmer_hub.frontend;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class HomeController {

    private final TokenStore tokenStore;

    public HomeController(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @GetMapping("/home")
    public String home(
        @RequestParam(name = "name", required = false, defaultValue = "Seguridad y Calidad en el Desarrollo") String name,
        Model model
    ) {
        model.addAttribute("name", name);
        return "Home";
    }

    @GetMapping("/")
    public String root(
        @RequestParam(name = "name", required = false, defaultValue = "Seguridad y Calidad en el Desarrollo") String name,
        Model model
    ) {
        model.addAttribute("name", name);
        return "Home";
    }

    @GetMapping("/greetings")
    public String greeting(
        @RequestParam(name = "name", required = false, defaultValue = "Juan González") String name,
        Model model
    ) {
        // Input validation
        if (name == null || name.length() > 100) {
            model.addAttribute("error", "Invalid name parameter");
            return "ErrorPage"; // Redirect to an error page
        }

        String url = "http://backend:8080/greetings";
        final var restTemplate = new RestTemplate();
        String token = tokenStore.getToken();

        // Secure logging
        System.out.println("Token: " + (token != null ? "****" : "No token available"));

        // Create headers and add the token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        // Add parameters to the URL
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("name", name);

        try {
            // Make the request with the token in the header and parameters in the URL
            ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class
            );

            // Check if the response is successful
            if (response.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("name", response.getBody());
            } else {
                model.addAttribute("error", "Error fetching greetings: " + response.getStatusCode());
                return "ErrorPage"; // Redirect to an error page
            }
        } catch (HttpClientErrorException e) {
            // Handle specific HTTP errors
            model.addAttribute("error", "Error fetching greetings: " + e.getStatusCode());
            return "ErrorPage"; // Redirect to an error page
        } catch (Exception e) {
            // Handle other exceptions
            model.addAttribute("error", "An unexpected error occurred.");
            return "ErrorPage"; // Redirect to an error page
        }

        return "Greetings";
    }
}
