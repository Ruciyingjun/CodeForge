package com.ruoyi.system.ai.domain;

import java.util.List;
import java.util.ArrayList;

public class ChatHistory {

    /**
     * 实现chat聊天功能的实体类
     */
    private String sessionId;
    private List<Message> messages;
    
    public ChatHistory(String sessionId) {
        this.sessionId = sessionId;
        this.messages = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void addMessage(Message message) {
        messages.add(message);
    }
}