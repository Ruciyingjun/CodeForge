package com.ruoyi.system.service;

import com.ruoyi.system.domain.AiChat;

public interface IAiChatService {
    /**
     * 发送消息到AI并获取响应
     * 
     * @param message 用户消息
     * @return AI响应
     */
    String sendMessage(String message);
}