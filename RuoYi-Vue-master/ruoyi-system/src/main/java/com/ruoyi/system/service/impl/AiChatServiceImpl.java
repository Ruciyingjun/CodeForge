package com.ruoyi.system.service.impl;

import com.google.gson.Gson;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.AiChat;
import com.ruoyi.system.service.IAiChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class AiChatServiceImpl implements IAiChatService {

    @Value("${ai.dashscope.apikey:}")
    private String apiKey;

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String MODEL = "qwen-turbo";

    @Override
    public String sendMessage(String message) {
        if (StringUtils.isEmpty(apiKey)) {
            return "API密钥未配置";
        }

        try {
            // 创建请求体
            AiChat.RequestBody requestBody = new AiChat.RequestBody(
                    MODEL,
                    new AiChat.Message[]{
                            new AiChat.Message("system", "You are a helpful assistant."),
                            new AiChat.Message("user", message)
                    }
            );

            // 将请求体转换为JSON
            Gson gson = new Gson();
            String jsonInputString = gson.toJson(requestBody);

            // 创建URL对象并设置请求头
            URL url = new URL(API_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Accept", "text/event-stream");
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + apiKey);
            httpURLConnection.setRequestProperty("X-DashScope-SSE", "enable");
            httpURLConnection.setRequestProperty("Connection", "keep-alive");
            httpURLConnection.setRequestProperty("Cache-Control", "no-cache");

            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            // 写入请求体
            try (OutputStream os = httpURLConnection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应码并处理
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode >= 400) {
                return "请求失败，错误码：" + responseCode;
            }

            // 处理流式响应
            StringBuilder fullContent = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String jsonData = line.substring(6);
                        if ("[DONE]".equals(jsonData)) {
                            break;
                        }

                        AiChat.Response response = gson.fromJson(jsonData, AiChat.Response.class);
                        if (response.getChoices() != null && response.getChoices().length > 0) {
                            String content = response.getChoices()[0].getMessage().getContent();
                            fullContent.append(content);
                        }
                    }
                }
            }

            return fullContent.toString();

        } catch (Exception e) {
            return "请求失败：" + e.getMessage();
        }
    }
}