package com.example.aiassistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAIConversation {

    private final String apiKey;
    private final String modelName;
    private String threadId;
    private final ArrayList<String> chatMessages;

    public OpenAIConversation(@Value("${openai.api-key}") String apiKey,
                              @Value("${openai.model-name}") String modelName) {
        this.apiKey = //TODO for some reason my openaikey has to be hard coded, you can try your;
        this.modelName = modelName;
        this.chatMessages = new ArrayList<>();
        this.createThread();
    }

    public String initializeThread(String assistantId) {
        try {
            URI uri = new URI("https://api.openai.com/v1/threads");
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");
            connection.setDoOutput(true);

            int status = connection.getResponseCode();
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(response.toString());
                    this.threadId = rootNode.get("id").asText();
                    return threadId;
                }
            } else {
                throw new RuntimeException("Failed to initialize thread. Status: " + status);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing thread: " + e.getMessage(), e);
        }
    }

    public String askQuestion(String context, String question) {
        try {
            // Add context and user message to chat history
            if (!chatMessages.contains(context)) {
                chatMessages.add("System: " + context);
            }
            chatMessages.add("User: " + question);

            // Send user message to OpenAI
            String response = sendUserMessageToThread(question);

            // Add AI's response to chat history
            chatMessages.add("Assistant: " + response);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error asking question: " + e.getMessage(), e);
        }
    }

    public List<String> generateSampleQuestions(String context, int count, int maxWords) {
        try {
            String instruction = String.format(
                    "Provide %d questions with a maximum of %d words each. Separate questions with '%%'.", count, maxWords);
            String response = askQuestion(context, instruction);
            return List.of(response.split("%%"));
        } catch (Exception e) {
            throw new RuntimeException("Error generating sample questions: " + e.getMessage(), e);
        }
    }

    public String getThreadId() {
        return threadId;
    }

    private void createThread() {
        try {
            URL url = new URL("https://api.openai.com/v1/threads");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");
            connection.setDoOutput(true);

            int status = connection.getResponseCode();
            System.out.println("Create Thread Status: " + status); // Log the status
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(response.toString());
                    this.threadId = rootNode.get("id").asText();
                    System.out.println("Thread ID: " + this.threadId); // Log the thread ID
                }
            } else {
                throw new RuntimeException("Failed to create thread. Status: " + status);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating thread: " + e.getMessage(), e);
        }
    }
    private String sendUserMessageToThread(String message) {
        try {
            URL url = new URL("https://api.openai.com/v1/threads/" + threadId + "/messages");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");
            connection.setDoOutput(true);

            // JSON payload
            String payload = "{ \"role\": \"user\", \"content\": \"" + message + "\" }";
            System.out.println("Request Payload: " + payload); // Log the request payload
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes());
            }

            int status = connection.getResponseCode();
            System.out.println("Response Status: " + status); // Log the response status
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return parseAssistantReply(response.toString());
                }
            } else {
                throw new RuntimeException("Failed to send user message. Status: " + status);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error sending user message: " + e.getMessage(), e);
        }
    }



    private String parseAssistantReply(String response) throws Exception {
        System.out.println("API Response: " + response); // Log the raw response for debugging
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);

        // Validate "content" field exists and contains data
        JsonNode contentArray = rootNode.path("content");
        if (contentArray.isMissingNode() || !contentArray.isArray() || contentArray.size() == 0) {
            throw new RuntimeException("Invalid API response: 'content' is missing or empty.");
        }

        // Extract the first item's text content
        JsonNode textNode = contentArray.get(0).path("text").path("value");
        if (textNode.isMissingNode() || textNode.asText().isEmpty()) {
            throw new RuntimeException("Invalid API response: 'text.value' is missing or empty.");
        }

        return textNode.asText();
    }


}
