package one.expressdev.geekmer_hub.frontend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
 
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;  
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;  
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
 

@WebMvcTest(HomeController.class)  
@WithMockUser  
 
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;  

    @MockBean  
    private TokenStore tokenStore;

    @MockBean  
    private RestTemplate restTemplate;

     
    private static final String MOCK_TOKEN = "Bearer test-token-123";
    private static final String GREETINGS_BACKEND_URL = "http://backend:8080/greetings";
    private static final String DEFAULT_HOME_NAME = "Seguridad y Calidad en el Desarrollo";
    private static final String DEFAULT_GREETING_NAME = "Juan González";

    @BeforeEach
    void setUp() {
         
         
        when(tokenStore.getToken()).thenReturn(MOCK_TOKEN);
    }

     

    @Test
    @DisplayName("GET /home should return Home view with default name")
    void home_shouldReturnHomeViewWithDefaultName() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())  
                .andExpect(view().name("Home"))  
                .andExpect(model().attributeExists("name"))  
                .andExpect(model().attribute("name", DEFAULT_HOME_NAME));  
    }

    @Test
    @DisplayName("GET /home?name=Test should return Home view with provided name")
    void home_withNameParam_shouldReturnHomeViewWithProvidedName() throws Exception {
        String testName = "Test Name";
        mockMvc.perform(get("/home").param("name", testName))
                .andExpect(status().isOk())
                .andExpect(view().name("Home"))
                .andExpect(model().attribute("name", testName));  
    }

     

    @Test
    @DisplayName("GET / should return Home view with default name")
    void root_shouldReturnHomeViewWithDefaultName() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("Home"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attribute("name", DEFAULT_HOME_NAME));
    }

     

    @Test
    @DisplayName("GET /greetings (success) should return Greetings view with backend response")
    void greeting_success_shouldReturnGreetingView() throws Exception {
        String requestName = "Alice";
        String backendResponse = "Hello Alice from Backend!";
        String expectedUrl = UriComponentsBuilder.fromHttpUrl(GREETINGS_BACKEND_URL)
                .queryParam("name", requestName)
                .toUriString();

         
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(backendResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),  
                eq(String.class)))
                .thenReturn(mockResponseEntity);

        mockMvc.perform(get("/greetings").param("name", requestName))
                .andExpect(status().isOk())
                .andExpect(view().name("Greetings"))  
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attribute("name", backendResponse));  
    }

    @Test
    @DisplayName("GET /greetings with default name (success) should return Greetings view")
    void greeting_defaultName_success_shouldReturnGreetingView() throws Exception {
        String backendResponse = "Hello " + DEFAULT_GREETING_NAME + " from Backend!";
        String expectedUrl = UriComponentsBuilder.fromHttpUrl(GREETINGS_BACKEND_URL)
                .queryParam("name", DEFAULT_GREETING_NAME)  
                .toUriString();

         
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(backendResponse, HttpStatus.OK);
         when(restTemplate.exchange(
                 eq(expectedUrl),
                 eq(HttpMethod.GET),
                 any(HttpEntity.class),
                 eq(String.class)))
                 .thenReturn(mockResponseEntity);

        mockMvc.perform(get("/greetings"))  
                .andExpect(status().isOk())
                .andExpect(view().name("Greetings"))  
                .andExpect(model().attribute("name", backendResponse));
    }

    @Test
    @DisplayName("GET /greetings with name longer than 100 chars should return ErrorPage")
    void greeting_nameTooLong_shouldReturnErrorPage() throws Exception {
        String longName = "a".repeat(101);  

        mockMvc.perform(get("/greetings").param("name", longName))
                .andExpect(status().isOk())  
                .andExpect(view().name("ErrorPage"))
                .andExpect(model().attributeExists("error"))
                 
                .andExpect(model().attribute("error", "Invalid name parameter: Name cannot be empty and must be 100 characters or less."));
    }

    @Test
    @DisplayName("GET /greetings when backend returns 4xx error should return ErrorPage")
    void greeting_backendHttpClientError_shouldReturnErrorPage() throws Exception {
        String requestName = "Bob";
         String expectedUrl = UriComponentsBuilder.fromHttpUrl(GREETINGS_BACKEND_URL)
                 .queryParam("name", requestName)
                 .toUriString();
        HttpStatus errorStatus = HttpStatus.UNAUTHORIZED;  

         
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new HttpClientErrorException(errorStatus, "Auth Failed"));

        mockMvc.perform(get("/greetings").param("name", requestName))
                .andExpect(status().isOk())  
                .andExpect(view().name("ErrorPage"))
                .andExpect(model().attributeExists("error"))
                 
                .andExpect(model().attribute("error", "Error fetching greetings: Backend returned status " + errorStatus));
    }

     @Test
     @DisplayName("GET /greetings when backend returns 5xx error should return ErrorPage")
     void greeting_backendHttpServerError_shouldReturnErrorPage() throws Exception {
         String requestName = "Charlie";
         String expectedUrl = UriComponentsBuilder.fromHttpUrl(GREETINGS_BACKEND_URL)
                 .queryParam("name", requestName)
                 .toUriString();
         HttpStatus errorStatus = HttpStatus.INTERNAL_SERVER_ERROR;  

          
         when(restTemplate.exchange(
                 eq(expectedUrl),
                 eq(HttpMethod.GET),
                 any(HttpEntity.class),
                 eq(String.class)))
                 .thenThrow(HttpClientErrorException.create(errorStatus, "Server Down", HttpHeaders.EMPTY, null, null));


         mockMvc.perform(get("/greetings").param("name", requestName))
                 .andExpect(status().isOk())  
                 .andExpect(view().name("ErrorPage"))
                 .andExpect(model().attributeExists("error"))
                  
                 .andExpect(model().attribute("error", "Error fetching greetings: Backend returned status " + errorStatus));
     }


    @Test
    @DisplayName("GET /greetings when network error occurs should return ErrorPage")
    void greeting_networkError_shouldReturnErrorPage() throws Exception {
        String requestName = "Dave";
        String expectedUrl = UriComponentsBuilder.fromHttpUrl(GREETINGS_BACKEND_URL)
                .queryParam("name", requestName)
                .toUriString();

         
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new RestClientException("Could not connect"));

        mockMvc.perform(get("/greetings").param("name", requestName))
                .andExpect(status().isOk())  
                .andExpect(view().name("ErrorPage"))
                .andExpect(model().attributeExists("error"))
                 
                .andExpect(model().attribute("error", "Could not connect to the backend service. Please try again later."));
    }

    @Test
    @DisplayName("GET /greetings when unexpected internal exception occurs should return ErrorPage")
    void greeting_unexpectedException_shouldReturnErrorPage() throws Exception {
        String requestName = "Frank";  
        String expectedUrl = UriComponentsBuilder.fromHttpUrl(GREETINGS_BACKEND_URL)
                .queryParam("name", requestName)
                .toUriString();

         
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new RuntimeException("Something broke internally"));

        mockMvc.perform(get("/greetings").param("name", requestName))
                .andExpect(status().isOk())  
                .andExpect(view().name("ErrorPage"))
                .andExpect(model().attributeExists("error"))
                 
                .andExpect(model().attribute("error", "An unexpected internal error occurred."));
    }


    @Test
    @DisplayName("GET /greetings when no token is available (success) should return Greetings view")
    void greeting_noToken_shouldStillCallBackend() throws Exception {
         
        when(tokenStore.getToken()).thenReturn(null);

        String requestName = "Eve";
        String backendResponse = "Hello Eve (no token provided)!";  
        String expectedUrl = UriComponentsBuilder.fromHttpUrl(GREETINGS_BACKEND_URL)
                .queryParam("name", requestName)
                .toUriString();

         
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(backendResponse, HttpStatus.OK);

         
         
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),  
                eq(String.class)))
                .thenReturn(mockResponseEntity);


        mockMvc.perform(get("/greetings").param("name", requestName))
                .andExpect(status().isOk())
                .andExpect(view().name("Greetings"))  
                .andExpect(model().attribute("name", backendResponse));

         
         
         
         
    }
@Test
    @DisplayName("GET /greetings when backend returns non-2xx success status (e.g., 3xx) should return ErrorPage")
    void greeting_backendNon2xxSuccessStatus_shouldReturnErrorPage() throws Exception {
        String requestName = "RedirectGuy";
        String expectedUrl = UriComponentsBuilder.fromHttpUrl(GREETINGS_BACKEND_URL)
                .queryParam("name", requestName)
                .toUriString();
        HttpStatus non2xxStatus = HttpStatus.FOUND;  

         
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("Redirecting...", non2xxStatus);  
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(mockResponseEntity);

        mockMvc.perform(get("/greetings").param("name", requestName))
                .andExpect(status().isOk())  
                .andExpect(view().name("ErrorPage"))  
                .andExpect(model().attributeExists("error"))
                 
                .andExpect(model().attribute("error", "Error fetching greetings: Received status " + non2xxStatus));
    }
}
