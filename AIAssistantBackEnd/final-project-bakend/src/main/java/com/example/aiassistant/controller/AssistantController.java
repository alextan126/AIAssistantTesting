package com.example.aiassistant.controller;

import com.example.aiassistant.service.AssistantConversation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class AssistantController {

    private final AssistantConversation assistantConversation;

    public AssistantController(AssistantConversation assistantConversation) {
        this.assistantConversation = assistantConversation;
    }

    @PostMapping("/assistants")
    public String createAssistant(@RequestParam String assistantName) {
        return assistantConversation.createAssistant(assistantName);
    }

    @PostMapping("/assistants/start")
    public String startConversation() {
        return assistantConversation.createThread();
    }

    @PostMapping("/assistants/ask")
    public String askAssistant(@RequestParam String context, @RequestParam String question) {
        return assistantConversation.askQuestion(context, question);
    }

    @GetMapping("/assistants/sample-questions")
    public List<String> getSampleQuestions(@RequestParam String context,
                                           @RequestParam int count,
                                           @RequestParam int maxWords) {
        return assistantConversation.generateSampleQuestions(context, count, maxWords);
    }

    @PostMapping("/assistants/upload")
    public String uploadFile(@RequestParam String filePath) {
        return assistantConversation.uploadFile(filePath);
    }
}
