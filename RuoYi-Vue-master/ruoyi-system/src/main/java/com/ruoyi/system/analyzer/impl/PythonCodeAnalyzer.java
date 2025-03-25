package com.ruoyi.system.analyzer.impl;

import com.ruoyi.system.analyzer.CodeAnalyzer;
import com.ruoyi.system.domain.CodeAnalysisReport;
import com.ruoyi.system.domain.CodeAnalysisReport.MetricResult;
import com.ruoyi.system.domain.CodeAnalysisReport.Issue;
import com.ruoyi.system.analyzer.utils.CodeAnalyzerUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("pythonCodeAnalyzer")  // 指定唯一名称
public class PythonCodeAnalyzer implements CodeAnalyzer {

    private static final int MAX_LINE_LENGTH = 79;  // PEP 8标准
    private static final Pattern FUNCTION_DEF = Pattern.compile("^\\s*def\\s+(\\w+)\\s*\\(");
    private static final Pattern CLASS_DEF = Pattern.compile("^\\s*class\\s+(\\w+)\\s*[:\\(]");
    private static final Pattern VARIABLE_DEF = Pattern.compile("^\\s*(\\w+)\\s*=");

    @Override
    public CodeAnalysisReport analyze(String code) {
        CodeAnalysisReport report = new CodeAnalysisReport();
        Map<String, MetricResult> metrics = new HashMap<>();

        try {
            // 1. 代码规范分析
            metrics.put("style", analyzeStyle(code));

            // 2. 复杂度分析
            metrics.put("complexity", analyzeComplexity(code));

            // 3. 命名规范分析
            metrics.put("naming", analyzeNaming(code));

            // 4. 文档和注释分析
            metrics.put("documentation", analyzeDocumentation(code));

            // 计算总分
            int totalScore = calculateTotalScore(metrics);
            report.setScore(totalScore);
            report.setMetrics(metrics);

        } catch (Exception e) {
            throw new RuntimeException("Python代码分析失败: " + e.getMessage());
        }

        return report;
    }

    private MetricResult analyzeStyle(String code) {
        MetricResult metric = new MetricResult();
        List<Issue> issues = new ArrayList<>();
        List<String> violations = new ArrayList<>();

        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            // 检查行长度
            if (CodeAnalyzerUtils.isLineTooLong(line, MAX_LINE_LENGTH)) {
                violations.add(CodeAnalyzerUtils.formatMessage("行长度超过79个字符", lineNumber));
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("行长度超过79个字符", lineNumber),
                        "warning",
                        "建议将长行拆分为多行，保持代码可读性"
                ));
            }

            // 检查缩进
            if (!line.trim().isEmpty()) {
                int indent = CodeAnalyzerUtils.getIndentationLevel(line);
                if (indent % 4 != 0) {
                    violations.add(CodeAnalyzerUtils.formatMessage("缩进不是4的倍数", lineNumber));
                    issues.add(createIssue(
                            CodeAnalyzerUtils.formatMessage("缩进不是4的倍数", lineNumber),
                            "warning",
                            "建议使用4个空格作为缩进单位"
                    ));
                }
            }

            // 检查空格使用
            if (line.contains("  ") && !line.trim().startsWith("#")) {
                violations.add(CodeAnalyzerUtils.formatMessage("存在多余的空格", lineNumber));
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("存在多余的空格", lineNumber),
                        "info",
                        "建议删除多余的空格"
                ));
            }

            // 检查操作符周围的空格
            if (line.matches(".*[^\\s][=+\\-*/<>][^\\s=].*")) {
                violations.add(CodeAnalyzerUtils.formatMessage("操作符周围缺少空格", lineNumber));
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("操作符周围缺少空格", lineNumber),
                        "info",
                        "建议在操作符前后添加空格"
                ));
            }
        }

        // 检查空行使用
        if (!CodeAnalyzerUtils.hasProperSpacing(code)) {
            issues.add(createIssue(
                    "空行使用不规范",
                    "info",
                    "建议在函数和类定义之间使用两个空行，方法定义之间使用一个空行"
            ));
        }

        int score = CodeAnalyzerUtils.calculateStyleScore(violations);
        metric.setScore(score);
        metric.setIssues(issues);
        return metric;
    }

    private MetricResult analyzeComplexity(String code) {
        MetricResult metric = new MetricResult();
        Map<String, Object> details = new HashMap<>();
        List<String> suggestions = new ArrayList<>();

        // 分析圈复杂度
        int cyclomaticComplexity = calculateCyclomaticComplexity(code);
        details.put("cyclomaticComplexity", cyclomaticComplexity);

        // 分析嵌套深度
        int maxNestingDepth = calculateMaxNestingDepth(code);
        details.put("maxNestingDepth", maxNestingDepth);

        // 分析函数长度
        Map<String, Integer> functionLengths = analyzeFunctionLengths(code);
        details.put("functionLengths", functionLengths);

        // 检查代码重复
        if (CodeAnalyzerUtils.hasCodeDuplication(code, 6)) {
            suggestions.add("发现重复代码块，建议提取为独立函数");
        }

        // 根据复杂度指标计算得分
        int score = CodeAnalyzerUtils.calculateComplexityScore(
                cyclomaticComplexity,
                maxNestingDepth,
                functionLengths
        );

        // 添加建议
        if (cyclomaticComplexity > 10) {
            suggestions.add("代码复杂度较高，建议拆分复杂的条件判断");
        }
        if (maxNestingDepth > 3) {
            suggestions.add("代码嵌套层级过深，建议提取函数或使用列表推导式");
        }

        metric.setScore(score);
        metric.setDetails(details);
        metric.setSuggestions(suggestions);
        return metric;
    }

    private MetricResult analyzeNaming(String code) {
        MetricResult metric = new MetricResult();
        List<Issue> issues = new ArrayList<>();
        List<String> violations = new ArrayList<>();

        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            // 检查函数命名
            Matcher funcMatcher = FUNCTION_DEF.matcher(line);
            if (funcMatcher.find()) {
                String funcName = funcMatcher.group(1);
                if (!funcName.matches("^[a-z_][a-z0-9_]*$")) {
                    String message = String.format("函数名'%s'不符合命名规范", funcName);
                    violations.add(CodeAnalyzerUtils.formatMessage(message, lineNumber));
                    issues.add(createIssue(
                            CodeAnalyzerUtils.formatMessage(message, lineNumber),
                            "warning",
                            "函数名应使用小写字母和下划线"
                    ));
                }
            }

            // 检查类命名
            Matcher classMatcher = CLASS_DEF.matcher(line);
            if (classMatcher.find()) {
                String className = classMatcher.group(1);
                if (!className.matches("^[A-Z][a-zA-Z0-9]*$")) {
                    String message = String.format("类名'%s'不符合命名规范", className);
                    violations.add(CodeAnalyzerUtils.formatMessage(message, lineNumber));
                    issues.add(createIssue(
                            CodeAnalyzerUtils.formatMessage(message, lineNumber),
                            "warning",
                            "类名应使用大驼峰命名法"
                    ));
                }
            }

            // 检查变量命名
            Matcher varMatcher = VARIABLE_DEF.matcher(line);
            if (varMatcher.find()) {
                String varName = varMatcher.group(1);
                if (!varName.matches("^[a-z_][a-z0-9_]*$")) {
                    String message = String.format("变量名'%s'不符合命名规范", varName);
                    violations.add(CodeAnalyzerUtils.formatMessage(message, lineNumber));
                    issues.add(createIssue(
                            CodeAnalyzerUtils.formatMessage(message, lineNumber),
                            "warning",
                            "变量名应使用小写字母和下划线"
                    ));
                }
            }
        }

        int score = CodeAnalyzerUtils.calculateNamingScore(violations);
        metric.setScore(score);
        metric.setIssues(issues);
        return metric;
    }

    private MetricResult analyzeDocumentation(String code) {
        MetricResult metric = new MetricResult();
        List<String> suggestions = new ArrayList<>();

        // 计算注释比例
        double commentRatio = CodeAnalyzerUtils.calculateCommentRatio(code);

        // 检查文档字符串
        boolean hasModuleDocstring = code.trim().startsWith("\"\"\"") || code.trim().startsWith("'''");
        boolean hasFunctionDocstrings = checkFunctionDocstrings(code);
        boolean hasClassDocstrings = checkClassDocstrings(code);

        int score = 100;

        if (commentRatio < 0.1) {
            score -= 20;
            suggestions.add("代码注释比例过低，建议添加适当的注释说明");
        }

        if (!hasModuleDocstring) {
            score -= 10;
            suggestions.add("缺少模块级别的文档字符串，建议添加模块说明");
        }

        if (!hasFunctionDocstrings) {
            score -= 15;
            suggestions.add("部分函数缺少文档字符串，建议为重要函数添加说明");
        }

        if (!hasClassDocstrings) {
            score -= 15;
            suggestions.add("部分类定义缺少文档字符串，建议为类添加说明");
        }

        Map<String, Object> details = new HashMap<>();
        details.put("commentRatio", commentRatio);
        details.put("hasModuleDocstring", hasModuleDocstring);
        details.put("hasFunctionDocstrings", hasFunctionDocstrings);
        details.put("hasClassDocstrings", hasClassDocstrings);

        metric.setScore(Math.max(0, score));
        metric.setDetails(details);
        metric.setSuggestions(suggestions);
        return metric;
    }

    private boolean checkFunctionDocstrings(String code) {
        String[] lines = code.split("\n");
        boolean foundFunction = false;
        boolean foundDocstring = false;

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("def ")) {
                if (foundFunction && !foundDocstring) {
                    return false;
                }
                foundFunction = true;
                foundDocstring = false;
            } else if (foundFunction && (line.startsWith("\"\"\"") || line.startsWith("'''"))) {
                foundDocstring = true;
            }
        }

        return !foundFunction || foundDocstring;
    }

    private boolean checkClassDocstrings(String code) {
        String[] lines = code.split("\n");
        boolean foundClass = false;
        boolean foundDocstring = false;

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("class ")) {
                if (foundClass && !foundDocstring) {
                    return false;
                }
                foundClass = true;
                foundDocstring = false;
            } else if (foundClass && (line.startsWith("\"\"\"") || line.startsWith("'''"))) {
                foundDocstring = true;
            }
        }

        return !foundClass || foundDocstring;
    }

    private int calculateCyclomaticComplexity(String code) {
        int complexity = 1;
        String[] lines = code.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("if ") ||
                    line.startsWith("elif ") ||
                    line.startsWith("for ") ||
                    line.startsWith("while ") ||
                    line.contains(" and ") ||
                    line.contains(" or ") ||
                    line.startsWith("except ")) {
                complexity++;
            }
        }

        return complexity;
    }

    private int calculateMaxNestingDepth(String code) {
        String[] lines = code.split("\n");
        int maxDepth = 0;
        int currentDepth = 0;

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            int indent = CodeAnalyzerUtils.getIndentationLevel(line);
            currentDepth = indent / 4;
            maxDepth = Math.max(maxDepth, currentDepth);
        }

        return maxDepth;
    }

    private Map<String, Integer> analyzeFunctionLengths(String code) {
        Map<String, Integer> functionLengths = new HashMap<>();
        String[] lines = code.split("\n");
        String currentFunction = null;
        int lineCount = 0;
        int currentIndent = -1;

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            int indent = CodeAnalyzerUtils.getIndentationLevel(line);
            Matcher funcMatcher = FUNCTION_DEF.matcher(line);

            if (funcMatcher.find()) {
                if (currentFunction != null) {
                    functionLengths.put(currentFunction, lineCount);
                }
                currentFunction = funcMatcher.group(1);
                currentIndent = indent;
                lineCount = 1;
            } else if (currentFunction != null) {
                if (indent > currentIndent) {
                    lineCount++;
                } else if (indent <= currentIndent && !line.trim().startsWith("#")) {
                    functionLengths.put(currentFunction, lineCount);
                    currentFunction = null;
                }
            }
        }

        if (currentFunction != null) {
            functionLengths.put(currentFunction, lineCount);
        }

        return functionLengths;
    }

    private Issue createIssue(String message, String severity, String suggestion) {
        Issue issue = new Issue();
        issue.setMessage(message);
        issue.setSeverity(severity);
        issue.setSuggestion(suggestion);
        return issue;
    }

    private int calculateTotalScore(Map<String, MetricResult> metrics) {
        return (int) metrics.values().stream()
                .mapToInt(MetricResult::getScore)
                .average()
                .orElse(0);
    }
}
