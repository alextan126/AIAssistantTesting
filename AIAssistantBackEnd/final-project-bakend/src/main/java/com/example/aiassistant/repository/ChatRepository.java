// src/main/java/com/example/aiassistant/repository/ChatRepository.java
package com.example.aiassistant.repository;

import com.example.aiassistant.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, String> {
    List<Chat> findByAssistantIdAndUserId(String assistantId, String userId);
}