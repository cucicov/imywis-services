package com.example.imywisservices;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ImywisServicesApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ImywisServicesApplication.class);
        app.addInitializers(applicationContext -> {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            try {
                Dotenv dotenv = Dotenv.configure()
                        .filename(".env.local")
                        .ignoreIfMissing()
                        .load();

                Map<String, Object> dotenvMap = new HashMap<>();
                dotenv.entries().forEach(entry -> dotenvMap.put(entry.getKey(), entry.getValue()));
                environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", dotenvMap));
            } catch (Exception e) {
                // .env.local is optional, continue without it
            }
        });
        app.run(args);
    }

}
