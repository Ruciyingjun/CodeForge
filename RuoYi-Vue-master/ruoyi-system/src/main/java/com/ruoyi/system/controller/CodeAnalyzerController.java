package com.ruoyi.system.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.system.service.CodeAnalyzerService;
import com.ruoyi.system.domain.CodeAnalyzeRequest;
import com.ruoyi.system.domain.CodeAnalysisReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/code-analyzer")
public class CodeAnalyzerController extends BaseController {

    @Autowired
    private CodeAnalyzerService codeAnalyzerService;

    @PreAuthorize("@ss.hasPermi('tool:code:list')")
    @GetMapping("/supported-languages")
    public AjaxResult getSupportedLanguages() {
        List<Map<String, String>> languages = codeAnalyzerService.getSupportedLanguages();
        return AjaxResult.success(languages);
    }

    @PreAuthorize("@ss.hasPermi('tool:code:list')")
    @Log(title = "代码分析", businessType = BusinessType.OTHER)
    @RepeatSubmit(interval = 5000)
    @PostMapping("/analyze")
    public AjaxResult analyzeCode(@RequestBody CodeAnalyzeRequest request) {
        try {
            CodeAnalysisReport report = codeAnalyzerService.analyzeCode(
                request.getLanguage(), 
                request.getCode(),
                request.getOptions()
            );
            return AjaxResult.success("代码分析成功", report);
        } catch (Exception e) {
            return AjaxResult.error("代码分析失败：" + e.getMessage());
        }
    }
} 