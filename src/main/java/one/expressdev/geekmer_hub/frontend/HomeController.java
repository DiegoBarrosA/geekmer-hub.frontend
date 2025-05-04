package one.expressdev.geekmer_hub.frontend;

import org.slf4j.Logger; // Import Logger for logging
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException; // Import a more general exception for network issues
import org.springframework.web.client.RestTemplate; // Import RestTemplate
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Controller for handling home, root, and greeting web requests.
 * Uses TokenStore to retrieve authentication tokens and RestTemplate
 * to communicate with a backend service.
 */
@Controller
public class HomeController {

    // Initialize a logger for this class
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    // Dependency for storing/retrieving authentication tokens
    private final TokenStore tokenStore;
    // Dependency for making HTTP requests (injected via constructor)
    private final RestTemplate restTemplate;

    /**
     * Constructor for HomeController. Dependencies are injected by Spring.
     * @param tokenStore The TokenStore bean.
     * @param restTemplate The RestTemplate bean.
     */
    public HomeController(TokenStore tokenStore, RestTemplate restTemplate) {
        // Assign injected dependencies to fields
        this.tokenStore = tokenStore;
        this.restTemplate = restTemplate;
    }

    /**
     * Handles requests to the /home path.
     * Adds a 'name' attribute to the model and returns the "Home" view.
     * @param name The name parameter from the request (optional, defaults provided).
     * @param model The Spring Model object to add attributes to.
     * @return The logical view name "Home".
     */
    @GetMapping("/home")
    public String home(
        @RequestParam(name = "name", required = false, defaultValue = "Seguridad y Calidad en el Desarrollo") String name,
        Model model
    ) {
        // Add the name attribute (either default or from request param) to the model
        model.addAttribute("name", name);
        // Return the name of the template to render (e.g., Home.html)
        return "Home";
    }

    /**
     * Handles requests to the root path (/).
     * Behaves identically to the /home endpoint.
     * Adds a 'name' attribute to the model and returns the "Home" view.
     * @param name The name parameter from the request (optional, defaults provided).
     * @param model The Spring Model object to add attributes to.
     * @return The logical view name "Home".
     */
    @GetMapping("/")
    public String root(
        @RequestParam(name = "name", required = false, defaultValue = "Seguridad y Calidad en el Desarrollo") String name,
        Model model
    ) {
        // Add the name attribute (either default or from request param) to the model
        model.addAttribute("name", name);
         // Return the name of the template to render (e.g., Home.html)
        return "Home";
    }

    /**
     * Handles requests to the /greetings path.
     * Validates the input name, retrieves a token, calls a backend service
     * using RestTemplate, and returns either the "Greetings" or "ErrorPage" view.
     * @param name The name parameter from the request (optional, defaults provided).
     * @param model The Spring Model object to add attributes to.
     * @return The logical view name "Greetings" on success, or "ErrorPage" on failure.
     */
    @GetMapping("/greetings")
    public String greeting(
        @RequestParam(name = "name", required = false, defaultValue = "Juan González") String name,
        Model model
    ) {
        // --- Input Validation ---
        // Check if the name parameter is null or exceeds the maximum allowed length
        if (name == null || name.length() > 100) {
            log.warn("Invalid name parameter received: length={}", (name != null ? name.length() : "null"));
            model.addAttribute("error", "Invalid name parameter: Name cannot be empty and must be 100 characters or less.");
            return "ErrorPage"; // Return the error view name
        }

        // Define the backend service URL
        String url = "http://backend:8080/greetings";

        // Retrieve the authentication token from the TokenStore
        String token = tokenStore.getToken();

        // Log token presence securely (avoid logging the actual token)
        log.debug("Token presence for backend call: {}", (token != null ? "Available" : "Not available"));
        // System.out.println("Token presence: " + (token != null ? "Available" : "Not available")); // Avoid System.out in production

        // --- Prepare HTTP Request ---
        // Create HttpHeaders object to hold request headers
        HttpHeaders headers = new HttpHeaders();
        // Only set the Authorization header if a token exists
        if (token != null && !token.trim().isEmpty()) {
             headers.set("Authorization", token);
        }
        // Create the HttpEntity, wrapping the headers (no request body needed for GET)
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers); // "parameters" is often ignored for GET but required by constructor

        // Build the full URL with query parameters using UriComponentsBuilder
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("name", name); // Add the name as a query parameter

        // --- Execute HTTP Request and Handle Response ---
        try {
            // Make the GET request using the injected RestTemplate instance
            // It sends the HttpEntity (containing headers) and expects a String response
            ResponseEntity<String> response = this.restTemplate.exchange(
                builder.toUriString(), // The complete URL with query params
                HttpMethod.GET,        // The HTTP method
                entity,                // The request entity (headers)
                String.class           // The expected response body type
            );

            // Check if the response status code is in the 2xx range (successful)
            if (response.getStatusCode().is2xxSuccessful()) {
                // Add the response body (the greeting) to the model
                model.addAttribute("name", response.getBody());
                // Return the success view name
                return "Greetings";
            } else {
                // Handle non-2xx responses that didn't throw an exception
                log.warn("Received non-successful status code {} from backend for name '{}'", response.getStatusCode(), name);
                model.addAttribute("error", "Error fetching greetings: Received status " + response.getStatusCode());
                return "ErrorPage"; // Return the error view name
            }
        // Catch specific HTTP client errors (like 4xx or 5xx responses)
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error fetching greetings for name '{}': {} - {}", name, e.getStatusCode(), e.getStatusText(), e);
            model.addAttribute("error", "Error fetching greetings: Backend returned status " + e.getStatusCode());
            return "ErrorPage"; // Return the error view name
        // Catch broader RestClientExceptions (network issues, timeouts, DNS errors, etc.)
        } catch (RestClientException e) {
             log.error("Network or communication error fetching greetings for name '{}'", name, e);
             model.addAttribute("error", "Could not connect to the backend service. Please try again later.");
             return "ErrorPage"; // Return the error view name
        // Catch any other unexpected exceptions
        } catch (Exception e) {
            log.error("Unexpected internal error during greeting request for name '{}'", name, e); // Log the full exception
            model.addAttribute("error", "An unexpected internal error occurred.");
            return "ErrorPage"; // Return the error view name
        }
    }
}
