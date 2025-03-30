package one.expressdev.geekmer_hub.frontend;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class HomeController {

    private TokenStore tokenStore;

    public HomeController(TokenStore tokenStore) {
        super();
        this.tokenStore = tokenStore;
    }

    @GetMapping("/home")
    public String home(
        @RequestParam(
            name = "name",
            required = false,
            defaultValue = "Seguridad y Calidad en el Desarrollo"
        ) String name,
        Model model
    ) {
        model.addAttribute("name", name);
        return "Home";
    }

    @GetMapping("/")
    public String root(
        @RequestParam(
            name = "name",
            required = false,
            defaultValue = "Seguridad y Calidad en el Desarrollo"
        ) String name,
        Model model
    ) {
        model.addAttribute("name", name);
        return "Home";
    }

    @GetMapping("/greetings")
    public String greeting(
        @RequestParam(
            name = "name",
            required = false,
            defaultValue = "Juan González"
        ) String name,
        Model model
    ) {
        System.out.println("Name: " + name);
        String url = "http://localhost:8080/greetings";
        final var restTemplate = new RestTemplate();
        String token = tokenStore.getToken();

        System.out.println("Token: " + token);

        // Crear los encabezados y añadir el token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity entity = new HttpEntity<>("parameters", headers);

        // Agregar parámetros a la URL
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
            url
        ).queryParam("name", name);

        // Realizar la petición con el token en el encabezado y los parámetros en la URL
        ResponseEntity response = restTemplate.exchange(
            builder.toUriString(),
            HttpMethod.GET,
            entity,
            String.class
        );

        System.out.println("Response: " + response);

        model.addAttribute("name", response.getBody());
        return "Greetings";
    }
}
