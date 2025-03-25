package com.ruoyi.system.service;

import java.util.Map;
import java.util.List;
import com.ruoyi.system.domain.CodeAnalysisReport;

public interface CodeAnalyzerService {
    /**
     * 分析代码
     * @param language 编程语言
     * @param code 代码内容
     * @param options 分析选项
     * @return 分析报告
     */
    CodeAnalysisReport analyzeCode(String language, String code, Map<String, Object> options);

    /**
     * 获取支持的语言列表
     * @return 语言列表
     */
    List<Map<String, String>> getSupportedLanguages();
} 