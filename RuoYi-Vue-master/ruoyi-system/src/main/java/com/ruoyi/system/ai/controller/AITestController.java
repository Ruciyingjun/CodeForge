package com.ruoyi.system.ai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.controller.BaseController;
import com.google.gson.Gson;
import com.ruoyi.system.ai.config.AiConfig;
import com.ruoyi.system.ai.domain.Message;
import com.ruoyi.system.ai.domain.AIRequestBody;

@RestController
@RequestMapping("/system/ai")
public class AITestController extends BaseController {
    /**
     * 一键测试连接模块。
     */
    
    // 删除内部的 RequestBody 类定义
    
    @PreAuthorize("@ss.hasPermi('system:ai:test')")
    @GetMapping("/test")
    public AjaxResult testAIConnection() {
        try {
            AIRequestBody requestBody = new AIRequestBody(
                AiConfig.MODEL_TYPE,
                new Message[] {
                    new Message("system", "You are a helpful assistant."),
                    new Message("user", "你好，请问你是谁？")
                }
            );

            // 将请求体转换为JSON
            Gson gson = new Gson();
            String jsonInputString = gson.toJson(requestBody);

            RestTemplate restTemplate = new RestTemplate();
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + AiConfig.API_KEY);  // 使用配置的API密钥
            headers.set("Accept", "application/json");
            
            HttpEntity<String> request = new HttpEntity<>(jsonInputString, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(AiConfig.API_URL, request, String.class);  // 使用配置的API地址
            
            return AjaxResult.success("API连接测试成功", "响应状态码: " + response.getStatusCodeValue() + 
                   "\n响应内容: " + response.getBody());
            
        } catch (Exception e) {
            return AjaxResult.error("API连接测试失败：" + e.getMessage());
        }
    }
}