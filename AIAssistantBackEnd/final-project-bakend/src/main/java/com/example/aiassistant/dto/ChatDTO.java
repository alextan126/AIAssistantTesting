// src/main/java/com/example/aiassistant/dto/ChatDTO.java
package com.example.aiassistant.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChatDTO {
    private String id;
    private String assistantId;
    private LocalDateTime createdAt;
    private List<MessageDTO> messages;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAssistantId() { return assistantId; }
    public void setAssistantId(String assistantId) { this.assistantId = assistantId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<MessageDTO> getMessages() { return messages; }
    public void setMessages(List<MessageDTO> messages) { this.messages = messages; }
}