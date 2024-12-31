package brk.oussama.gemini;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GeminiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeminiApplication.class, args);
    }

}
