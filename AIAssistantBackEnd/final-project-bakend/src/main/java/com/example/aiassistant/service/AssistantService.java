package com.example.aiassistant.service;

import com.example.aiassistant.dto.*;
import com.example.aiassistant.model.*;
import com.example.aiassistant.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssistantService {
    private final AssistantRepository assistantRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public AssistantService(
            AssistantRepository assistantRepository,
            ChatRepository chatRepository,
            MessageRepository messageRepository) {
        this.assistantRepository = assistantRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    public UserSessionDTO createUserSession() {
        UserSessionDTO session = new UserSessionDTO();
        session.setUserId(UUID.randomUUID().toString());
        return session;
    }

    public List<AssistantDTO> getAllAssistants() {
        return assistantRepository.findAll().stream()
                .map(this::convertToAssistantDTO)
                .collect(Collectors.toList());
    }

    public AssistantDTO createAssistant(AssistantDTO request) {
        Assistant assistant = new Assistant();
        assistant.setName(request.getName());
        assistant.setDescription(request.getDescription());
        return convertToAssistantDTO(assistantRepository.save(assistant));
    }

    public List<ChatDTO> getChats(String assistantId, String userId) {
        return chatRepository.findByAssistantIdAndUserId(assistantId, userId).stream()
                .map(this::convertToChatDTO)
                .collect(Collectors.toList());
    }

    public ChatDTO createChat(String assistantId, String userId) {
        Assistant assistant = assistantRepository.findById(assistantId)
                .orElseThrow(() -> new RuntimeException("Assistant not found"));

        Chat chat = new Chat();
        chat.setAssistant(assistant);
        chat.setUserId(userId);
        return convertToChatDTO(chatRepository.save(chat));
    }

    public List<MessageDTO> getMessages(String chatId, String userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return messageRepository.findByChatIdOrderByTimestampAsc(chatId).stream()
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());
    }

    public MessageDTO addMessage(String chatId, String userId, MessageDTO messageDTO) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        Message message = new Message();
        message.setChat(chat);
        message.setContent(messageDTO.getContent());
        message.setSender("user");

        return convertToMessageDTO(messageRepository.save(message));
    }

    private AssistantDTO convertToAssistantDTO(Assistant assistant) {
        AssistantDTO dto = new AssistantDTO();
        dto.setId(assistant.getId());
        dto.setName(assistant.getName());
        dto.setDescription(assistant.getDescription());
        dto.setCreatedAt(assistant.getCreatedAt());
        return dto;
    }

    private ChatDTO convertToChatDTO(Chat chat) {
        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setAssistantId(chat.getAssistant().getId());
        dto.setCreatedAt(chat.getCreatedAt());
        return dto;
    }

    private MessageDTO convertToMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSender(message.getSender());
        dto.setTimestamp(message.getTimestamp());
        return dto;
    }
}