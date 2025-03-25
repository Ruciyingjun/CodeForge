package com.ruoyi.system.ai.controller;

import com.google.gson.Gson;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
// 删除原有的 Message 导入
// import org.apache.logging.log4j.message.Message;
import com.ruoyi.system.ai.config.AiConfig;
import com.ruoyi.system.ai.domain.AIRequestBody;
import com.ruoyi.system.ai.domain.Message;  // 使用我们自定义的 Message 类
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static com.ruoyi.system.ai.config.AiConfig.API_URL;

@RestController
@RequestMapping("/system/ai")
public class AiController extends BaseController {
    
    @PostMapping("/generateCode")
    public AjaxResult generateCode(@RequestBody GenerateCodeRequest request) {
        try {
            AIRequestBody requestBody = new AIRequestBody(
                AiConfig.MODEL_TYPE,
                new Message[] {
                    new Message("system", "You are a coding assistant..."),
                    new Message("user", "使用" + request.getLanguage() + "编写：" + request.getPrompt())
                }
            );

            // 配置 RestTemplate 的超时时间
            RestTemplate restTemplate = new RestTemplate();
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(30000); // 连接超时 30 秒
            requestFactory.setReadTimeout(30000);    // 读取超时 30 秒
            restTemplate.setRequestFactory(requestFactory);
            
            // 将请求体转换为JSON
            Gson gson = new Gson();
            String jsonInputString = gson.toJson(requestBody);

//            RestTemplate restTemplate = new RestTemplate();
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + AiConfig.API_KEY);
            
            headers.set("Accept", "application/json");
            
            HttpEntity<String> httpRequest = new HttpEntity<>(jsonInputString, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, httpRequest, String.class);
            
            // 解析响应JSON
            Map<String, Object> jsonResponse = gson.fromJson(response.getBody(), Map.class);
            Map<String, Object> choices = ((List<Map<String, Object>>) jsonResponse.get("choices")).get(0);
            Map<String, String> message = (Map<String, String>) choices.get("message");
            String content = message.get("content");
            
            // 提取代码块
            String code = extractCodeBlock(content);
            
            GenerateCodeResponse codeResponse = new GenerateCodeResponse();
            codeResponse.setCode(code);
            codeResponse.setExplanation("代码生成成功");
            
            return AjaxResult.success(codeResponse);
        } catch (Exception e) {
            logger.error("代码生成失败", e);
            return AjaxResult.error("代码生成失败：" + e.getMessage());
        }
    }

    /**
     * 从AI响应中提取代码块
     */
    private String extractCodeBlock(String content) {
        // 查找代码块的起始和结束位置
        int start = content.indexOf("```");
        if (start != -1) {
            start = content.indexOf("\n", start) + 1;
            int end = content.indexOf("```", start);
            if (end != -1) {
                return content.substring(start, end).trim();
            }
        }
        // 如果没有找到代码块标记，返回原始内容
        return content.trim();
    }
}

/**
 * 代码生成请求参数
 */
class GenerateCodeRequest {
    private String language;
    private String prompt;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}

/**
 * 代码生成响应结果
 */
class GenerateCodeResponse {
    private String code;
    private String explanation;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}