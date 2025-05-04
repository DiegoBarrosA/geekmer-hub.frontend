package one.expressdev.geekmer_hub.frontend;

import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;  
import org.springframework.web.client.RestTemplate;  
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final TokenStore tokenStore;
    private final RestTemplate restTemplate;

    public HomeController(TokenStore tokenStore, RestTemplate restTemplate) {
        this.tokenStore = tokenStore;
        this.restTemplate = restTemplate;
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
        if (name == null || name.length() > 100) {
            log.warn("Invalid name parameter received: length={}", (name != null ? name.length() : "null"));
            model.addAttribute("error", "Invalid name parameter: Name cannot be empty and must be 100 characters or less.");
            return "ErrorPage";  
        }

        String url = "http://backend:8080/greetings";

        String token = tokenStore.getToken();

        log.debug("Token presence for backend call: {}", (token != null ? "Available" : "Not available"));
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.trim().isEmpty()) {
             headers.set("Authorization", token);
        }
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);  

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("name", name);  

        try {
            ResponseEntity<String> response = this.restTemplate.exchange(
                builder.toUriString(),  
                HttpMethod.GET,         
                entity,                 
                String.class            
            );

             
            if (response.getStatusCode().is2xxSuccessful()) {
                 
                model.addAttribute("name", response.getBody());
                 
                return "Greetings";
            } else {
                 
                log.warn("Received non-successful status code {} from backend for name '{}'", response.getStatusCode(), name);
                model.addAttribute("error", "Error fetching greetings: Received status " + response.getStatusCode());
                return "ErrorPage";  
            }
         
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error fetching greetings for name '{}': {} - {}", name, e.getStatusCode(), e.getStatusText(), e);
            model.addAttribute("error", "Error fetching greetings: Backend returned status " + e.getStatusCode());
            return "ErrorPage";  
         
        } catch (RestClientException e) {
             log.error("Network or communication error fetching greetings for name '{}'", name, e);
             model.addAttribute("error", "Could not connect to the backend service. Please try again later.");
             return "ErrorPage";  
         
        } catch (Exception e) {
            log.error("Unexpected internal error during greeting request for name '{}'", name, e);  
            model.addAttribute("error", "An unexpected internal error occurred.");
            return "ErrorPage";  
        }
    }
}
