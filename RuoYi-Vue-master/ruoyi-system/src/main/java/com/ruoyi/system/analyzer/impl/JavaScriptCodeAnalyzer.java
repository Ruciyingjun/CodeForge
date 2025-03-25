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

@Component("javascriptCodeAnalyzer")  // 指定唯一名称
public class JavaScriptCodeAnalyzer implements CodeAnalyzer {

    private static final int MAX_LINE_LENGTH = 100;  // JavaScript通用标准
    private static final Pattern FUNCTION_DEF = Pattern.compile("(function\\s+(\\w+)|const\\s+(\\w+)\\s*=\\s*(?:function|\\(.*\\)\\s*=>))");
    private static final Pattern CLASS_DEF = Pattern.compile("class\\s+(\\w+)");
    private static final Pattern VAR_DEF = Pattern.compile("(var|let|const)\\s+(\\w+)\\s*=");

    @Override
    public CodeAnalysisReport analyze(String code) {
        CodeAnalysisReport report = new CodeAnalysisReport();
        Map<String, MetricResult> metrics = new HashMap<>();

        try {
            // 1. 代码规范分析
            metrics.put("style", analyzeStyle(code));

            // 2. 复杂度分析
            metrics.put("complexity", analyzeComplexity(code));

            // 3. ES6+特性分析
            metrics.put("es6", analyzeES6Features(code));

            // 4. 最佳实践分析
            metrics.put("bestPractices", analyzeBestPractices(code));

            // 计算总分
            int totalScore = calculateTotalScore(metrics);
            report.setScore(totalScore);
            report.setMetrics(metrics);

        } catch (Exception e) {
            throw new RuntimeException("JavaScript代码分析失败: " + e.getMessage());
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
                violations.add(CodeAnalyzerUtils.formatMessage("行长度超过100个字符", lineNumber));
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("行长度超过100个字符", lineNumber),
                        "warning",
                        "建议将长行拆分，提高可读性"
                ));
            }

            // 检查分号使用
            if (!line.trim().isEmpty() && !line.endsWith(";") &&
                    !line.endsWith("{") && !line.endsWith("}") &&
                    !line.endsWith(")") && !line.trim().startsWith("import")) {
                violations.add(CodeAnalyzerUtils.formatMessage("缺少分号", lineNumber));
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("缺少分号", lineNumber),
                        "info",
                        "建议在语句末尾添加分号"
                ));
            }

            // 检查var使用
            if (line.contains("var ")) {
                violations.add(CodeAnalyzerUtils.formatMessage("使用var声明变量", lineNumber));
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("使用var声明变量", lineNumber),
                        "warning",
                        "建议使用let或const代替var"
                ));
            }

            // 检查空格使用
            if (line.matches(".*[^\\s][=+\\-*/<>][^\\s=].*")) {
                violations.add(CodeAnalyzerUtils.formatMessage("操作符周围缺少空格", lineNumber));
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("操作符周围缺少空格", lineNumber),
                        "info",
                        "建议在操作符前后添加空格"
                ));
            }

            // 检查缩进
            if (!line.trim().isEmpty()) {
                int indent = CodeAnalyzerUtils.getIndentationLevel(line);
                if (indent % 2 != 0) {
                    violations.add(CodeAnalyzerUtils.formatMessage("缩进不规范", lineNumber));
                    issues.add(createIssue(
                            CodeAnalyzerUtils.formatMessage("缩进不规范", lineNumber),
                            "warning",
                            "建议使用2个空格的缩进"
                    ));
                }
            }
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

        // 检查回调嵌套
        int callbackNesting = analyzeCallbackNesting(code);
        details.put("callbackNesting", callbackNesting);

        // 计算得分
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
            suggestions.add("代码嵌套层级过深，建议提取函数或使用Promise/async-await");
        }
        if (callbackNesting > 2) {
            suggestions.add("回调嵌套层级过多，建议使用Promise或async/await重构");
        }

        metric.setScore(score);
        metric.setDetails(details);
        metric.setSuggestions(suggestions);
        return metric;
    }

    private MetricResult analyzeES6Features(String code) {
        MetricResult metric = new MetricResult();
        List<String> suggestions = new ArrayList<>();
        Map<String, Object> details = new HashMap<>();

        boolean usesArrowFunctions = code.contains("=>");
        boolean usesDestructuring = code.contains("{...") || code.matches(".*\\{[^{]*}\\s*=.*");
        boolean usesTemplateStrings = code.contains("`");
        boolean usesAsync = code.contains("async ") || code.contains("await ");
        boolean usesPromise = code.contains("new Promise") || code.contains(".then(");
        boolean usesModules = code.contains("import ") || code.contains("export ");

        details.put("usesArrowFunctions", usesArrowFunctions);
        details.put("usesDestructuring", usesDestructuring);
        details.put("usesTemplateStrings", usesTemplateStrings);
        details.put("usesAsync", usesAsync);
        details.put("usesPromise", usesPromise);
        details.put("usesModules", usesModules);

        int score = 100;

        if (!usesArrowFunctions) {
            score -= 10;
            suggestions.add("建议使用箭头函数替代传统函数表达式");
        }

        if (!usesDestructuring) {
            score -= 10;
            suggestions.add("建议使用解构赋值简化代码");
        }

        if (!usesTemplateStrings && code.contains("' + ")) {
            score -= 10;
            suggestions.add("建议使用模板字符串替代字符串拼接");
        }

        if (!usesAsync && usesPromise) {
            score -= 10;
            suggestions.add("建议使用async/await替代Promise链式调用");
        }

        if (!usesModules) {
            score -= 10;
            suggestions.add("建议使用ES模块化组织代码");
        }

        metric.setScore(score);
        metric.setDetails(details);
        metric.setSuggestions(suggestions);
        return metric;
    }

    private MetricResult analyzeBestPractices(String code) {
        MetricResult metric = new MetricResult();
        List<Issue> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            // 检查console.log使用
            if (line.contains("console.log")) {
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("使用了console.log", lineNumber),
                        "warning",
                        "生产环境中应该移除或使用正式的日志系统"
                ));
            }

            // 检查==使用
            if (line.contains("==") || line.contains("!=")) {
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("使用了非严格相等运算符", lineNumber),
                        "warning",
                        "建议使用=== 和!==进行比较"
                ));
            }

            // 检查eval使用
            if (line.contains("eval(")) {
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("使用了eval函数", lineNumber),
                        "error",
                        "eval函数存在安全风险，应避免使用"
                ));
            }

            // 检查with语句使用
            if (line.contains("with(")) {
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("使用了with语句", lineNumber),
                        "error",
                        "with语句会导致作用域混乱，应避免使用"
                ));
            }
        }

        int score = 100 - (issues.size() * 10);
        score = Math.max(0, score);

        metric.setScore(score);
        metric.setIssues(issues);
        metric.setSuggestions(suggestions);
        return metric;
    }

    private int analyzeCallbackNesting(String code) {
        int maxNesting = 0;
        int currentNesting = 0;
        String[] lines = code.split("\n");

        for (String line : lines) {
            if (line.contains("callback(") || line.contains(".then(") || line.contains("=>")) {
                currentNesting++;
                maxNesting = Math.max(maxNesting, currentNesting);
            }
            if (line.contains("}")) {
                currentNesting = Math.max(0, currentNesting - 1);
            }
        }

        return maxNesting;
    }

    private int calculateCyclomaticComplexity(String code) {
        int complexity = 1;
        String[] lines = code.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.contains("if ") ||
                    line.contains("else ") ||
                    line.contains("for ") ||
                    line.contains("while ") ||
                    line.contains("case ") ||
                    line.contains("catch ") ||
                    line.contains("&&") ||
                    line.contains("||") ||
                    line.contains("?.") ||
                    line.contains("??")) {
                complexity++;
            }
        }

        return complexity;
    }

    private Map<String, Integer> analyzeFunctionLengths(String code) {
        Map<String, Integer> functionLengths = new HashMap<>();
        String[] lines = code.split("\n");
        String currentFunction = null;
        int lineCount = 0;
        int braceCount = 0;

        for (String line : lines) {
            line = line.trim();
            Matcher matcher = FUNCTION_DEF.matcher(line);

            if (matcher.find()) {
                if (currentFunction == null) {
                    currentFunction = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
                    lineCount = 1;
                    braceCount = 1;
                }
            } else if (currentFunction != null) {
                lineCount++;
                braceCount += line.chars().filter(ch -> ch == '{').count();
                braceCount -= line.chars().filter(ch -> ch == '}').count();

                if (braceCount == 0) {
                    functionLengths.put(currentFunction, lineCount);
                    currentFunction = null;
                }
            }
        }

        return functionLengths;
    }

    private int calculateMaxNestingDepth(String code) {
        int maxDepth = 0;
        int currentDepth = 0;

        for (char c : code.toCharArray()) {
            if (c == '{') {
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (c == '}') {
                currentDepth--;
            }
        }

        return maxDepth;
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