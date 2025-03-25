package com.ruoyi.web.controller.tool;

import com.ruoyi.system.controller.CodeAnalyzerController;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.ICodeAnalyzerService;
import com.ruoyi.system.domain.CodeAnalyzeRequest;
import com.ruoyi.system.domain.CodeAnalysisReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/tool/codeanalyzer")
public class CodeAnalyzerUSERController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(CodeAnalyzerController.class);

    @Autowired
    private ICodeAnalyzerService codeAnalyzerService;

    @PreAuthorize("@ss.hasPermi('tool:code:list')")
    @PostMapping("/analyze")
    public AjaxResult analyze(@RequestBody CodeAnalyzeRequest request) {
        try {
            CodeAnalysisReport report = codeAnalyzerService.analyze(request);
            return AjaxResult.success(report);
        } catch (Exception e) {
            log.error("代码分析失败", e);
            return AjaxResult.error("代码分析失败：" + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('tool:code:query')")
    @GetMapping("/supported-languages")
    public AjaxResult getSupportedLanguages() {
        return AjaxResult.success(codeAnalyzerService.getSupportedLanguages());
    }
}