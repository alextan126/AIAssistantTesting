// src/main/java/com/example/aiassistant/dto/AssistantDTO.java
package com.example.aiassistant.dto;

import java.time.LocalDateTime;

public class AssistantDTO {
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}