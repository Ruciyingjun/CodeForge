package com.ruoyi.system.analyzer;

import com.ruoyi.system.domain.CodeAnalysisReport;

public interface CodeAnalyzer {
    CodeAnalysisReport analyze(String code);
} 