package com.example.aiassistant.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssistantConversation {

    private static final String BASE_URL = "https://api.openai.com/v1";

    private final String apiKey;
    private final String modelName;

    private String assistantId;
    private String threadId;
    private final List<String> chatMessages;

    public AssistantConversation(@Value("${openai.api-key}") String apiKey,
                                 @Value("${openai.model-name}") String modelName) {
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.chatMessages = new ArrayList<>();
    }

    public String createAssistant(String assistantName) {
        try {
            URL url = new URL(BASE_URL + "/assistants");
            HttpURLConnection connection = createConnection(url, "POST");

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", modelName);
            payload.put("name", assistantName);
            payload.put("instructions", "You are a helpful assistant.");
            payload.put("tools", List.of(Map.of("type", "file_search")));

            sendPayload(connection, payload);

            String response = getResponse(connection);
            System.out.println("API Response: " + response); // Log the API response
            JsonNode rootNode = new ObjectMapper().readTree(response);
            JsonNode idNode = rootNode.get("id");
            if (idNode == null) {
                throw new RuntimeException("The 'id' field is missing in the API response: " + response);
            }
            this.assistantId = idNode.asText();
            return assistantId;
        } catch (Exception e) {
            throw new RuntimeException("Error creating assistant: " + e.getMessage(), e);
        }
    }


    public String createThread() {
        try {
            URL url = new URL(BASE_URL + "/threads");
            HttpURLConnection connection = createConnection(url, "POST");

            String response = getResponse(connection);
            JsonNode rootNode = new ObjectMapper().readTree(response);
            this.threadId = rootNode.get("id").asText();
            return threadId;
        } catch (Exception e) {
            throw new RuntimeException("Error creating thread: " + e.getMessage(), e);
        }
    }

    public String askQuestion(String context, String question) {
        try {
            if (!chatMessages.contains(context)) {
                chatMessages.add("System: " + context);
            }
            chatMessages.add("User: " + question);

            String userMessageId = sendMessageToThread(question);
            String response = getAssistantReply();

            chatMessages.add("Assistant: " + response);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error asking question: " + e.getMessage(), e);
        }
    }

    public List<String> generateSampleQuestions(String context, int count, int maxWords) {
        try {
            String instruction = String.format("Provide %d questions with a maximum of %d words each. Separate questions with '%%'.", count, maxWords);
            String response = askQuestion(context, instruction);
            return List.of(response.split("%%"));
        } catch (Exception e) {
            throw new RuntimeException("Error generating sample questions: " + e.getMessage(), e);
        }
    }

    public String uploadFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: " + filePath);
            }

            URL url = new URL(BASE_URL + "/files");
            HttpURLConnection connection = createMultipartConnection(url);

            String boundary = "Boundary-" + UUID.randomUUID();
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
                writer.append("Content-Type: application/octet-stream\r\n\r\n");
                writer.flush();

                Files.copy(file.toPath(), os);
                os.flush();

                writer.append("\r\n").append("--").append(boundary).append("--").append("\r\n");
                writer.flush();
            }

            String response = getResponse(connection);
            JsonNode rootNode = new ObjectMapper().readTree(response);
            return rootNode.get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
        }
    }

    private String sendMessageToThread(String message) throws IOException {
        URL url = new URL(BASE_URL + "/threads/" + threadId + "/messages");
        HttpURLConnection connection = createConnection(url, "POST");

        Map<String, String> payload = Map.of("role", "user", "content", message);
        sendPayload(connection, payload);

        String response = getResponse(connection);
        JsonNode rootNode = new ObjectMapper().readTree(response);
        return rootNode.get("id").asText();
    }

    private String getAssistantReply() throws IOException, InterruptedException {
        int maxRetries = 3; // Maximum number of retries
        int delayMs = 5000; // Delay in milliseconds between retries
        String activeRunId = hasActiveRun(threadId); // Check if there is an active run

        for (int i = 0; i < maxRetries; i++) {
            String runId;

            if (activeRunId != null) {
                // Use the existing active run
                System.out.println("Using existing active run with ID: " + activeRunId);
                runId = activeRunId;
            } else {
                // Start a new run if no active run exists
                System.out.println("No active run found. Starting a new run...");
                URL url = new URL(BASE_URL + "/threads/" + threadId + "/runs");
                HttpURLConnection connection = createConnection(url, "POST");

                Map<String, Object> payload = Map.of("assistant_id", assistantId, "stream", false);
                sendPayload(connection, payload);

                String response = getResponse(connection);
                System.out.println("New Run Response: " + response); // Log the API response for debugging

                JsonNode rootNode = new ObjectMapper().readTree(response);
                runId = rootNode.path("id").asText();
                activeRunId = runId; // Update activeRunId for the next iteration
            }

            // Check the status of the run
            String response = getRunStatus(threadId, runId);
            JsonNode rootNode = new ObjectMapper().readTree(response);
            System.out.println("RootNode found: " + rootNode.toString());
            String status = rootNode.path("status").asText();
            System.out.println("Run Status: " + status); // Print the run status

            if ("completed".equals(status)) {
                JsonNode contentNode = rootNode.path("content");

                //TODO debug find where the response is stored
                JsonNode toolResources = rootNode.path("tool_resources");
                if (!toolResources.isMissingNode() && !toolResources.isEmpty()) {
                    System.out.println("Tool resources found: " + toolResources.toString());
                    return toolResources.toString(); // Return tool results
                }

                JsonNode metadata = rootNode.path("metadata");
                if (!metadata.isMissingNode() && !metadata.isEmpty()) {
                    System.out.println("Metadata found: " + metadata.toString());
                    return metadata.toString(); // Return metadata as response
                }

                JsonNode usage = rootNode.path("usage");
                int completionTokens = usage.path("completion_tokens").asInt();
                if (completionTokens > 0) {
                    System.out.println("Tokens used for completion: " + completionTokens);
                    // Further exploration needed if no clear response field
                }

                System.out.println("Full Response: " + rootNode.toString());


                //end
                if (!contentNode.isArray() || contentNode.size() == 0) {
                    throw new RuntimeException("Invalid response: 'content' is missing or not an array - " + response);
                }

                JsonNode textNode = contentNode.get(0).path("text").path("value");
                if (textNode.isMissingNode()) {
                    throw new RuntimeException("Invalid response: 'text.value' is missing - " + response);
                }

                return textNode.asText();
            } else if ("failed".equals(status)) {
                throw new RuntimeException("The run failed. Last error: " + rootNode.path("last_error").asText());
            } else if ("queued".equals(status) || "in_progress".equals(status)) {
                System.out.println("Run is in progress or queued. Retrying...");
                Thread.sleep(delayMs);
            } else {
                throw new RuntimeException("Unexpected status: " + status);
            }
        }

        throw new RuntimeException("Timeout: The assistant reply did not complete within the expected time.");
    }

    private String getRunStatus(String threadId, String runId) throws IOException {
        URL url = new URL(BASE_URL + "/threads/" + threadId + "/runs/" + runId);
        HttpURLConnection connection = createConnection(url, "GET");

        String response = getResponse(connection);
        System.out.println("Run Status Response: " + response); // Log the response for debugging
        return response;
    }


    private String hasActiveRun(String threadId) throws IOException {
        URL url = new URL(BASE_URL + "/threads/" + threadId + "/runs");
        HttpURLConnection connection = createConnection(url, "GET");

        String response = getResponse(connection);
        System.out.println("Checking Active Runs Response: " + response); // Log the API response

        JsonNode rootNode = new ObjectMapper().readTree(response);
        String status = rootNode.path("status").asText();
        String runId = rootNode.path("id").asText(); // Extract run ID

        System.out.println("Active Run Status: " + status); // Print the active run status

        if ("queued".equals(status) || "in_progress".equals(status)) {
            return runId; // Return the run ID if active
        }

        return null; // No active run
    }

    private void cancelActiveRun(String threadId, String runId) throws IOException {
        URL url = new URL(BASE_URL + "/threads/" + threadId + "/runs/" + runId + "/cancel");
        HttpURLConnection connection = createConnection(url, "POST");

        String response = getResponse(connection);
        System.out.println("Cancel Run Response: " + response); // Log the cancel response
    }



    private HttpURLConnection createConnection(URL url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("OpenAI-Beta", "assistants=v2");
        connection.setDoOutput(true);
        return connection;
    }

    private HttpURLConnection createMultipartConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("OpenAI-Beta", "assistants=v2");
        connection.setDoOutput(true);
        return connection;
    }

    private void sendPayload(HttpURLConnection connection, Object payload) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (OutputStream os = connection.getOutputStream()) {
            mapper.writeValue(os, payload);
        }
    }

    private String getResponse(HttpURLConnection connection) throws IOException {
        try (InputStream is = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }
}
