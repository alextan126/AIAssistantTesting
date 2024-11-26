package com.example.aiassistant.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssistantConversation {

    private final OpenAIConversation openAIConversation;

    public AssistantConversation(OpenAIConversation openAIConversation) {
        this.openAIConversation = openAIConversation;
    }


    public String startConversation(String assistantId) {
        return openAIConversation.initializeThread(assistantId);
    }

    public String askQuestion(String context, String question) {
        return openAIConversation.askQuestion(context, question);
    }


    public List<String> getSampleQuestions(String context, int count, int maxWords) {
        return openAIConversation.generateSampleQuestions(context, count, maxWords);
    }
}
