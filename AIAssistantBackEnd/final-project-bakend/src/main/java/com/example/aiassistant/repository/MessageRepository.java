// src/main/java/com/example/aiassistant/repository/MessageRepository.java
package com.example.aiassistant.repository;

import com.example.aiassistant.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByChatIdOrderByTimestampAsc(String chatId);
}