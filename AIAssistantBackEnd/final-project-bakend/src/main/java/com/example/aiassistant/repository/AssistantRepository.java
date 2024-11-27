// src/main/java/com/example/aiassistant/repository/AssistantRepository.java
package com.example.aiassistant.repository;

import com.example.aiassistant.model.Assistant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantRepository extends JpaRepository<Assistant, String> {
}