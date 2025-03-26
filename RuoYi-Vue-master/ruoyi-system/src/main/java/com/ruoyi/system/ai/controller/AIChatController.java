package com.ruoyi.system.ai.controller;

import com.ruoyi.system.ai.config.AiConfig;
import com.ruoyi.system.ai.domain.AIRequestBody;
import com.ruoyi.system.ai.domain.ChatHistory;
import com.ruoyi.system.ai.domain.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.controller.BaseController;
import com.google.gson.Gson;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system/ai/chat")
public class AIChatController extends BaseController {
    
    private static final Logger log = LoggerFactory.getLogger(AIChatController.class);
    
    // 添加 chatSessions 成员变量
    private final Map<String, ChatHistory> chatSessions = new ConcurrentHashMap<>();
    
    @PostMapping("/send")
    public AjaxResult sendMessage(@RequestBody Map<String, String> params) {
        try {
            String sessionId = params.get("sessionId");
            String content = params.get("content");
            String systemRole = params.get("systemRole");
            
            // 获取或创建会话历史
            ChatHistory history = chatSessions.computeIfAbsent(
                sessionId, k -> {
                    ChatHistory newHistory = new ChatHistory(sessionId);
                    // 添加系统角色消息
                    newHistory.addMessage(new Message("system", systemRole));
                    return newHistory;
                }
            );
            
            // 添加用户消息
            Message userMessage = new Message("user", content);
            history.addMessage(userMessage);
            
            // 构建请求体
            AIRequestBody requestBody = new AIRequestBody(
                AiConfig.MODEL_TYPE,
                history.getMessages().toArray(new Message[0])
            );
            
            // 将请求体转换为JSON
            Gson gson = new Gson();
            String jsonInputString = gson.toJson(requestBody);

            RestTemplate restTemplate = new RestTemplate();
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + AiConfig.API_KEY);
            headers.set("Accept", "application/json");
            
            HttpEntity<String> request = new HttpEntity<>(jsonInputString, headers);
            
            // 发送请求前打印
            log.info("发送到AI的请求：{}", jsonInputString);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(AiConfig.API_URL, request, String.class);
            
            // 接收响应后打印
            log.info("收到AI响应：{}", response.getBody());
            
            // 解析AI响应
            Map<String, Object> jsonResponse = gson.fromJson(response.getBody(), Map.class);
            Map<String, Object> choices = ((List<Map<String, Object>>) jsonResponse.get("choices")).get(0);
            Map<String, String> message = (Map<String, String>) choices.get("message");
            String aiReply = message.get("content");

            // 添加AI响应到历史记录
            Message aiResponse = new Message("assistant", aiReply);
            history.addMessage(aiResponse);
            
            return AjaxResult.success("对话成功", aiResponse);
            
        } catch (Exception e) {
            log.error("对话失败", e);
            return AjaxResult.error("对话失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/history/{sessionId}")
    public AjaxResult getChatHistory(@PathVariable String sessionId) {
        ChatHistory history = chatSessions.get(sessionId);
        if (history == null) {
            return AjaxResult.error("会话不存在");
        }
        // 过滤掉系统消息
        List<Message> displayMessages = history.getMessages().stream()
                .filter(msg -> !"system".equals(msg.getRole()))
                .collect(Collectors.toList());
        return AjaxResult.success(displayMessages);
    }
}