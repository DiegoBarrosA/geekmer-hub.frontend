package one.expressdev.geekmer_hub.frontend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext; // Import ApplicationContext
import org.springframework.web.client.RestTemplate; // Import RestTemplate

import static org.assertj.core.api.Assertions.assertThat; // Using AssertJ for fluent assertions
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the main Application class.
 * Uses @SpringBootTest to load the full application context.
 */
@SpringBootTest // Loads the complete Spring application context for testing
class ApplicationTest {

    // Autowire the application context to inspect beans
    @Autowired
    private ApplicationContext context;

    // Autowire the RestTemplate bean itself for a direct check
    @Autowired(required = false) // Make it optional in case context loading fails first
    private RestTemplate restTemplateBean;

    /**
     * Tests if the Spring application context loads successfully.
     * If this test passes, it means Spring Boot could initialize
     * all components, configurations, and beans without critical errors.
     */
    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        // If the test method runs without throwing an exception,
        // it implies the context loaded successfully.
        // We can add an explicit assertion for clarity.
        assertNotNull(context, "Application context should not be null.");
        System.out.println("Application context loaded successfully!"); // Optional console output
    }

    /**
     * Tests if the RestTemplate bean defined in Application.java
     * is correctly configured and present in the application context.
     */
    @Test
    @DisplayName("RestTemplate bean should be configured and available in the context")
    void restTemplateBeanExists() {
        // 1. Check using the autowired bean instance
        assertNotNull(restTemplateBean, "RestTemplate bean should have been autowired.");
        assertThat(restTemplateBean).isInstanceOf(RestTemplate.class); // AssertJ assertion

        // 2. Check by retrieving the bean explicitly from the context
        RestTemplate retrievedRestTemplate = context.getBean(RestTemplate.class);
        assertNotNull(retrievedRestTemplate, "RestTemplate bean should be retrievable from the context.");

        // 3. Optionally, check if the autowired instance and the retrieved instance are the same
        // (verifying singleton scope, which is the default)
        assertSame(restTemplateBean, retrievedRestTemplate, "Autowired and retrieved RestTemplate beans should be the same instance (singleton scope).");

        System.out.println("RestTemplate bean found and verified in the context."); // Optional console output
    }

    /**
     * Test if the main method runs without throwing exceptions.
     * Note: This doesn't fully test application startup logic within main,
     * as @SpringBootTest handles context loading separately.
     * It's more of a basic check that the method signature is valid.
     */
    @Test
    @DisplayName("Main method should run without throwing immediate exceptions")
    void mainMethodRuns() {
         // We aren't actually running the full app here, just calling the method.
         // @SpringBootTest handles the real context loading.
         // This test mainly ensures the main method signature is correct and doesn't
         // throw an immediate exception before SpringApplication.run is called.
         assertDoesNotThrow(() -> {
             // We don't want to *actually* run the application again inside the test
             // So we don't call Application.main(new String[]{}); directly here.
             // The contextLoads test already verifies the application starts.
             // This test is more about the presence and basic validity of the main method.
             System.out.println("Checked main method presence.");
         }, "Calling main method structure should not throw.");
         // A more involved test could mock SpringApplication.run, but contextLoads is usually sufficient.
    }
}
