package com.ruoyi.system.service;

import com.ruoyi.system.domain.CodeAnalyzeRequest;
import com.ruoyi.system.domain.CodeAnalysisReport;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ICodeAnalyzerService {
    /**
     * 分析代码
     * @param request 分析请求
     * @return 分析报告
     */
    CodeAnalysisReport analyze(CodeAnalyzeRequest request);
    
    /**
     * 分析代码（异步）
     * @param language 编程语言
     * @param code 代码内容
     * @return 异步分析结果
     */
    CompletableFuture<CodeAnalysisReport> analyzeCodeAsync(String language, String code);
    
    /**
     * 获取支持的语言列表
     * @return 语言列表
     */
    List<Map<String, String>> getSupportedLanguages();
    
    /**
     * 获取指定语言的分析规则
     * @param language 编程语言
     * @return 分析规则
     */
    Map<String, Object> getLanguageRules(String language);
} 