package com.example.aiassistant.controller;

import com.example.aiassistant.dto.*;
import com.example.aiassistant.service.AssistantConversation;
import com.example.aiassistant.service.AssistantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class AssistantController {
    private final AssistantService assistantService;
    private final AssistantConversation assistantConversation;

    public AssistantController(AssistantService assistantService, AssistantConversation assistantConversation) {
        this.assistantService = assistantService;
        this.assistantConversation = assistantConversation;
    }

    // User session endpoints
    @PostMapping("/login")
    public UserSessionDTO login() {
        return assistantService.createUserSession();
    }

    // Assistant management endpoints
    @GetMapping("/assistants")
    public List<AssistantDTO> getAllAssistants() {
        return assistantService.getAllAssistants();
    }

    @PostMapping("/assistants")
    public AssistantDTO createAssistant(@RequestBody AssistantDTO request) {
        return assistantService.createAssistant(request);
    }

    // Chat management endpoints
    @GetMapping("/assistants/{assistantId}/chats")
    public List<ChatDTO> getChats(
            @PathVariable String assistantId,
            @RequestHeader("User-Id") String userId) {
        return assistantService.getChats(assistantId, userId);
    }

    @PostMapping("/assistants/{assistantId}/chats")
    public ChatDTO createChat(
            @PathVariable String assistantId,
            @RequestHeader("User-Id") String userId) {
        return assistantService.createChat(assistantId, userId);
    }

    // Message management endpoints
    @GetMapping("/chats/{chatId}/messages")
    public List<MessageDTO> getMessages(
            @PathVariable String chatId,
            @RequestHeader("User-Id") String userId) {
        return assistantService.getMessages(chatId, userId);
    }

    @PostMapping("/chats/{chatId}/messages")
    public MessageDTO addMessage(
            @PathVariable String chatId,
            @RequestHeader("User-Id") String userId,
            @RequestBody MessageDTO message) {
        return assistantService.addMessage(chatId, userId, message);
    }

    // New endpoints integrated with AssistantConversation
    @PostMapping("/assistants/{assistantId}/start")
    public String startConversation(@PathVariable String assistantId) {
        return assistantConversation.startConversation(assistantId);
    }

    @PostMapping("/assistants/ask")
    public String askAssistant(@RequestParam String context, @RequestParam String question) {
        return assistantConversation.askQuestion(context, question);
    }

    @GetMapping("/assistants/sample-questions")
    public List<String> getSampleQuestions(@RequestParam String context,
                                           @RequestParam int count,
                                           @RequestParam int maxWords) {
        return assistantConversation.getSampleQuestions(context, count, maxWords);
    }
}
