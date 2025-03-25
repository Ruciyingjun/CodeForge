package com.ruoyi.system.ai.domain;

public class AIRequestBody {
    private String model;
    private Message[] messages;

    public AIRequestBody(String model, Message[] messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Message[] getMessages() {
        return messages;
    }

    public void setMessages(Message[] messages) {
        this.messages = messages;
    }
}