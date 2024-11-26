package com.example.aiassistant.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class AssistantConversation {

    private final OpenAIConversation openAIConversation;

    public AssistantConversation(OpenAIConversation openAIConversation) {
        this.openAIConversation = openAIConversation;
    }

    public String startConversation(String assistantId) {
        // Initialize a conversation thread using OpenAIConversation
        return openAIConversation.initializeThread(assistantId);
    }

    public String askQuestion(String context, String question) {
        // Delegate question-answering to OpenAIConversation
        return openAIConversation.askQuestion(context, question);
    }

    public List<String> getSampleQuestions(String context, int count, int maxWords) {
        // Generate sample questions using OpenAIConversation
        return openAIConversation.generateSampleQuestions(context, count, maxWords);
    }
}
