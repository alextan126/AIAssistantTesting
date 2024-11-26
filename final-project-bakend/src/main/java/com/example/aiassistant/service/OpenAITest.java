package com.example.demo;

import okhttp3.*;
import java.io.IOException;

public class OpenAITest {

    public static void main(String[] args) {
        String apiKey = "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"; // Replace with your key
        String url = "https://api.openai.com/v1/chat/completions";

        OkHttpClient client = new OkHttpClient();

        String requestBody = "{\n" +
                "  \"model\": \"gpt-3.5-turbo\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},\n" +
                "    {\"role\": \"user\", \"content\": \"Hello, what can you do?\"}\n" +
                "  ]\n" +
                "}";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Response: " + response.body().string());
            } else {
                System.out.println("Error: " + response.code() + " - " + response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
