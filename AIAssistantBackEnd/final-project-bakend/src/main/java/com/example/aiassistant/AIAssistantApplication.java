package com.example.aiassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//TODO: need to set up api key in app.yaml or application.properties
@SpringBootApplication
public class AIAssistantApplication {
    public static void main(String[] args) {
        SpringApplication.run(AIAssistantApplication.class, args);
    }
}