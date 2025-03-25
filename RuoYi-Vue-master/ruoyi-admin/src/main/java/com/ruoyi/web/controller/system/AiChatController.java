package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.IAiChatService;

/**
 * AI对话功能
 */
@RestController
@RequestMapping("/system/ai")
public class AiChatController {
    @Autowired
    private IAiChatService aiChatService;

    /**
     * 发送消息
     */
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody String message) {
        String response = aiChatService.sendMessage(message);
        return AjaxResult.success(response);
    }
}