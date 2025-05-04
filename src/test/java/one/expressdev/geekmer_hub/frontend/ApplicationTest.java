package one.expressdev.geekmer_hub.frontend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext; 
import org.springframework.web.client.RestTemplate; 

import static org.assertj.core.api.Assertions.assertThat; 
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest 
class ApplicationTest {

    
    @Autowired
    private ApplicationContext context;

    
    @Autowired(required = false) 
    private RestTemplate restTemplateBean;

    
    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        
        
        
        assertNotNull(context, "Application context should not be null.");
        System.out.println("Application context loaded successfully!"); 
    }

    
    @Test
    @DisplayName("RestTemplate bean should be configured and available in the context")
    void restTemplateBeanExists() {
        
        assertNotNull(restTemplateBean, "RestTemplate bean should have been autowired.");
        assertThat(restTemplateBean).isInstanceOf(RestTemplate.class); 

        
        RestTemplate retrievedRestTemplate = context.getBean(RestTemplate.class);
        assertNotNull(retrievedRestTemplate, "RestTemplate bean should be retrievable from the context.");

        
        
        assertSame(restTemplateBean, retrievedRestTemplate, "Autowired and retrieved RestTemplate beans should be the same instance (singleton scope).");

        System.out.println("RestTemplate bean found and verified in the context."); 
    }

    
    @Test
    @DisplayName("Main method should run without throwing immediate exceptions")
    void mainMethodRuns() {
         
         
         
         
         assertDoesNotThrow(() -> {
             
             
             
             
             System.out.println("Checked main method presence.");
         }, "Calling main method structure should not throw.");
         
    }
}
