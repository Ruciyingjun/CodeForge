package com.ruoyi.system.ai.service;

import com.google.gson.Gson;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.AiChat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * AI代码生成服务
 */
@Service
public class AiCodeService {

    /**
     * 生成代码
     *
     * @param language 编程语言
     * @param prompt  代码需求描述
     * @return 生成的代码和说明
     */
    @Value("${ai.dashscope.apikey:}")
    private String apiKey;

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String MODEL = "qwen-turbo";

    public CodeGenerationResult generateCode(String language, String prompt) {
        if (StringUtils.isEmpty(apiKey)) {
            CodeGenerationResult result = new CodeGenerationResult();
            result.setCode("// API密钥未配置");
            result.setExplanation("请配置API密钥");
            return result;
        }

        try {
            String systemPrompt = String.format("你是一个代码生成助手，请使用%s语言，根据以下需求生成代码：%s", language, prompt);
            
            // 创建请求体
            AiChat.RequestBody requestBody = new AiChat.RequestBody(
                    MODEL,
                    new AiChat.Message[]{
                            new AiChat.Message("system", systemPrompt),
                            new AiChat.Message("user", "请生成代码，并在代码后面用中文解释代码的主要功能。")
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
                CodeGenerationResult result = new CodeGenerationResult();
                result.setCode("// 请求失败");
                result.setExplanation("API请求失败，错误码：" + responseCode);
                return result;
            }

            // 处理流式响应
            StringBuilder codeContent = new StringBuilder();
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
                            codeContent.append(content);
                        }
                    }
                }
            }

            // 解析生成的内容，分离代码和解释
            String[] parts = codeContent.toString().split("\n\n", 2);
            CodeGenerationResult result = new CodeGenerationResult();
            result.setCode(parts[0]);
            result.setExplanation(parts.length > 1 ? parts[1] : "代码生成完成");
            return result;

        } catch (Exception e) {
            CodeGenerationResult result = new CodeGenerationResult();
            result.setCode("// 生成失败");
            result.setExplanation("代码生成失败：" + e.getMessage());
            return result;
        }
    }
    
    private String generateJavaExample() {
        return "public class Example {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World\");\n    }\n}";
    }
    
    private String generatePythonExample() {
        return "def main():\n    print(\"Hello World\")\n\nif __name__ == \"__main__\":\n    main()";
    }
    
    private String generateJavaScriptExample() {
        return "function greet() {\n    console.log(\"Hello World\");\n}\n\ngreet();";
    }
    
    private String generateVueExample() {
        return "<template>\n  <div>\n    <h1>{{ message }}</h1>\n  </div>\n</template>\n\n<script>\nexport default {\n  data() {\n    return {\n      message: 'Hello World'\n    }\n  }\n}\n</script>";
    }
    
    private String generateSQLExample() {
        return "CREATE TABLE example (\n    id INT PRIMARY KEY,\n    name VARCHAR(50),\n    created_at TIMESTAMP\n);\n\nINSERT INTO example (id, name, created_at)\nVALUES (1, 'Test', CURRENT_TIMESTAMP);";
    }
}