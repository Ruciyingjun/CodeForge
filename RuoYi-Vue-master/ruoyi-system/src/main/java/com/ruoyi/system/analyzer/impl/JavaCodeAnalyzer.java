package com.ruoyi.system.analyzer.impl;

import com.ruoyi.system.analyzer.CodeAnalyzer;
import com.ruoyi.system.domain.CodeAnalysisReport;
import com.ruoyi.system.domain.CodeAnalysisReport.MetricResult;
import com.ruoyi.system.domain.CodeAnalysisReport.Issue;
import com.ruoyi.system.analyzer.utils.CodeAnalyzerUtils;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("javaCodeAnalyzer")
public class JavaCodeAnalyzer implements CodeAnalyzer {

    private static final int MAX_LINE_LENGTH = 120;  // Java通用标准
    private static final Pattern CLASS_DEF = Pattern.compile("\\s*(public|private|protected)?\\s*class\\s+(\\w+)");
    private static final Pattern METHOD_DEF = Pattern.compile("\\s*(public|private|protected)\\s+\\w+\\s+(\\w+)\\s*\\(");
    private static final Pattern FIELD_DEF = Pattern.compile("\\s*(public|private|protected)\\s+\\w+\\s+(\\w+)\\s*[=;]");
    private static final Pattern INTERFACE_DEF = Pattern.compile("\\s*(public|private|protected)?\\s*interface\\s+(\\w+)");

    private static final Logger log = LoggerFactory.getLogger(JavaCodeAnalyzer.class);

    @Override
    public CodeAnalysisReport analyze(String code) {
        CodeAnalysisReport report = new CodeAnalysisReport();
        try {
            // 执行具体的分析逻辑
            Map<String, MetricResult> metrics = new HashMap<>();
            
            // 分析代码规范
            metrics.put("style", analyzeStyle(code));
            
            // 分析复杂度
            metrics.put("complexity", analyzeComplexity(code));
            
            // 分析命名规范
            metrics.put("naming", analyzeNaming(code));
            
            // 设置分析结果
            report.setMetrics(metrics);
            report.setScore(calculateOverallScore(metrics));
            
            return report;
        } catch (Exception e) {
            log.error("Java代码分析失败", e);
            throw new RuntimeException("Java代码分析失败：" + e.getMessage());
        }
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
                violations.add(CodeAnalyzerUtils.formatMessage("行长度超过120个字符", lineNumber));
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("行长度超过120个字符", lineNumber),
                        "warning",
                        "建议将长行拆分，提高可读性"
                ));
            }

            // 检查缩进
            if (!line.trim().isEmpty()) {
                int indent = CodeAnalyzerUtils.getIndentationLevel(line);
                if (indent % 4 != 0) {
                    violations.add(CodeAnalyzerUtils.formatMessage("缩进不规范", lineNumber));
                    issues.add(createIssue(
                            CodeAnalyzerUtils.formatMessage("缩进不规范", lineNumber),
                            "warning",
                            "建议使用4个空格的缩进"
                    ));
                }
            }

            // 检查大括号风格
            if (line.contains("{")) {
                if (!line.contains(" {")) {
                    violations.add(CodeAnalyzerUtils.formatMessage("大括号前缺少空格", lineNumber));
                    issues.add(createIssue(
                            CodeAnalyzerUtils.formatMessage("大括号前缺少空格", lineNumber),
                            "info",
                            "建议在大括号前添加空格"
                    ));
                }
            }

            // 检查操作符空格
            if (line.matches(".*[^\\s][=+\\-*/<>][^\\s=].*")) {
                violations.add(CodeAnalyzerUtils.formatMessage("操作符周围缺少空格", lineNumber));
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("操作符周围缺少空格", lineNumber),
                        "info",
                        "建议在操作符前后添加空格"
                ));
            }

            // 检查分号后的空格
            if (line.matches(".*;[^\\s}].*")) {
                violations.add(CodeAnalyzerUtils.formatMessage("分号后缺少空格", lineNumber));
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("分号后缺少空格", lineNumber),
                        "info",
                        "建议在分号后添加空格"
                ));
            }

            // 检查逗号后的空格
            if (line.matches(".*,[^\\s].*")) {
                violations.add(CodeAnalyzerUtils.formatMessage("逗号后缺少空格", lineNumber));
                issues.add(createIssue(
                        CodeAnalyzerUtils.formatMessage("逗号后缺少空格", lineNumber),
                        "info",
                        "建议在逗号后添加空格"
                ));
            }
        }

        // 检查空行使用
        if (!CodeAnalyzerUtils.hasProperSpacing(code)) {
            issues.add(createIssue(
                    "空行使用不规范",
                    "info",
                    "建议在类、方法之间使用空行分隔"
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

        // 分析方法长度
        Map<String, Integer> methodLengths = analyzeMethodLengths(code);
        details.put("methodLengths", methodLengths);

        // 检查代码重复
        if (CodeAnalyzerUtils.hasCodeDuplication(code, 6)) {
            suggestions.add("发现重复代码块，建议提取为公共方法");
        }

        // 计算得分
        int score = CodeAnalyzerUtils.calculateComplexityScore(
                cyclomaticComplexity,
                maxNestingDepth,
                methodLengths
        );

        // 添加建议
        if (cyclomaticComplexity > 15) {
            suggestions.add("方法圈复杂度过高，建议拆分为多个小方法");
        }
        if (maxNestingDepth > 3) {
            suggestions.add("代码嵌套层级过深，建议提取方法或使用卫语句");
        }

        // 检查方法长度
        methodLengths.forEach((method, length) -> {
            if (length > 50) {
                suggestions.add(String.format("方法'%s'过长(%d行)，建议拆分为多个小方法", method, length));
            }
        });

        metric.setScore(score);
        metric.setDetails(details);
        metric.setSuggestions(suggestions);
        return metric;
    }

    private MetricResult analyzeOOP(String code) {
        MetricResult metric = new MetricResult();
        List<String> suggestions = new ArrayList<>();
        Map<String, Object> details = new HashMap<>();
        List<Issue> issues = new ArrayList<>();

        // 分析类的内聚性
        int methodCount = countMethods(code);
        int fieldCount = countFields(code);
        double cohesion = calculateCohesion(methodCount, fieldCount);
        details.put("methodCount", methodCount);
        details.put("fieldCount", fieldCount);
        details.put("cohesion", cohesion);

        // 分析继承深度
        int inheritanceDepth = calculateInheritanceDepth(code);
        details.put("inheritanceDepth", inheritanceDepth);

        // 检查封装性
        boolean hasPublicFields = checkPublicFields(code);
        details.put("hasPublicFields", hasPublicFields);

        // 检查接口实现
        int interfaceCount = countInterfaces(code);
        details.put("interfaceCount", interfaceCount);

        int score = 100;

        // 评分规则
        if (methodCount > 20) {
            score -= 20;
            suggestions.add("类中方法过多，建议拆分为多个类");
        }

        if (inheritanceDepth > 3) {
            score -= 15;
            suggestions.add("继承层次过深，建议使用组合替代继承");
        }

        if (hasPublicFields) {
            score -= 10;
            suggestions.add("存在public字段，建议使用private配合getter/setter");
            issues.add(createIssue(
                    "存在public字段",
                    "warning",
                    "违反封装原则，建议使用private配合getter/setter"
            ));
        }

        if (cohesion < 0.5) {
            score -= 15;
            suggestions.add("类的内聚性较低，建议将不相关的功能拆分到不同的类中");
        }

        if (interfaceCount == 0) {
            suggestions.add("未实现任何接口，建议考虑面向接口编程");
        } else if (interfaceCount > 5) {
            score -= 10;
            suggestions.add("实现接口过多，可能违反接口隔离原则");
        }

        metric.setScore(Math.max(0, score));
        metric.setDetails(details);
        metric.setSuggestions(suggestions);
        metric.setIssues(issues);
        return metric;
    }

    private MetricResult analyzeExceptionHandling(String code) {
        MetricResult metric = new MetricResult();
        List<Issue> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        String[] lines = code.split("\n");
        boolean hasTryBlock = false;
        boolean hasCatchBlock = false;
        boolean hasFinallyBlock = false;
        boolean hasEmptyCatch = false;
        boolean hasResourceInTry = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.startsWith("try")) {
                hasTryBlock = true;
                // 检查是否使用了try-with-resources
                if (line.contains("try (")) {
                    hasResourceInTry = true;
                }
            } else if (line.startsWith("catch")) {
                hasCatchBlock = true;
                // 检查是否是空的catch块
                if (i + 1 < lines.length && lines[i + 1].trim().equals("}")) {
                    hasEmptyCatch = true;
                    issues.add(createIssue(
                            String.format("第%d行存在空的catch块", i + 1),
                            "error",
                            "空的catch块可能掩盖重要异常，建议至少记录日志"
                    ));
                }
                // 检查异常捕获的粒度
                if (line.contains("catch (Exception") || line.contains("catch (Throwable")) {
                    issues.add(createIssue(
                            String.format("第%d行捕获了所有异常", i + 1),
                            "warning",
                            "建议捕获具体的异常类型，避免捕获所有异常"
                    ));
                }
            } else if (line.startsWith("finally")) {
                hasFinallyBlock = true;
            }

            // 检查异常处理方式
            if (line.contains(".printStackTrace()")) {
                issues.add(createIssue(
                        String.format("第%d行直接打印异常堆栈", i + 1),
                        "warning",
                        "建议使用日志框架记录异常信息"
                ));
            }
        }

        int score = 100;

        if (hasTryBlock && !hasCatchBlock) {
            score -= 30;
            suggestions.add("存在try块但缺少catch块");
        }

        if (hasEmptyCatch) {
            score -= 20;
            suggestions.add("存在空的catch块，建议添加异常处理逻辑");
        }

        if (!hasFinallyBlock && hasTryBlock) {
            suggestions.add("建议使用finally块进行资源清理");
        }

        if (!hasResourceInTry && hasTryBlock) {
            suggestions.add("建议使用try-with-resources语法自动关闭资源");
        }

        metric.setScore(Math.max(0, score));
        metric.setIssues(issues);
        metric.setSuggestions(suggestions);
        return metric;
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
                    line.contains("||")) {
                complexity++;
            }
        }

        return complexity;
    }

    private Map<String, Integer> analyzeMethodLengths(String code) {
        Map<String, Integer> methodLengths = new HashMap<>();
        String[] lines = code.split("\n");
        String currentMethod = null;
        int lineCount = 0;
        int braceCount = 0;

        for (String line : lines) {
            line = line.trim();

            if (line.matches(".*\\s+\\w+\\s*\\(.*\\)\\s*\\{")) {
                if (currentMethod == null) {
                    currentMethod = extractMethodName(line);
                    lineCount = 1;
                    braceCount = 1;
                }
            } else if (currentMethod != null) {
                lineCount++;
                braceCount += line.chars().filter(ch -> ch == '{').count();
                braceCount -= line.chars().filter(ch -> ch == '}').count();

                if (braceCount == 0) {
                    methodLengths.put(currentMethod, lineCount);
                    currentMethod = null;
                }
            }
        }

        return methodLengths;
    }

    private String extractMethodName(String line) {
        Pattern pattern = Pattern.compile("\\s*(public|private|protected)?\\s+\\w+\\s+(\\w+)\\s*\\(");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return "unknown";
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

    private int countMethods(String code) {
        Matcher matcher = METHOD_DEF.matcher(code);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

    private int countFields(String code) {
        Matcher matcher = FIELD_DEF.matcher(code);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

    private int countInterfaces(String code) {
        int count = 0;
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.contains("implements ")) {
                count += line.split("implements")[1].split(",").length;
            }
        }
        return count;
    }

    private double calculateCohesion(int methodCount, int fieldCount) {
        if (methodCount == 0 || fieldCount == 0) return 1.0;
        return Math.min(1.0, (double) methodCount / fieldCount);
    }

    private int calculateInheritanceDepth(String code) {
        int depth = 0;
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.contains("extends")) depth++;
        }
        return depth;
    }

    private boolean checkPublicFields(String code) {
        return code.matches(".*public\\s+(?!class|interface|enum)\\w+\\s+\\w+.*");
    }

    private Issue createIssue(String message, String severity, String suggestion) {
        Issue issue = new Issue();
        issue.setMessage(message);
        issue.setSeverity(severity);
        issue.setSuggestion(suggestion);
        return issue;
    }

    private int calculateOverallScore(Map<String, MetricResult> metrics) {
        return (int) metrics.values().stream()
                .mapToInt(MetricResult::getScore)
                .average()
                .orElse(0);
    }

    private MetricResult analyzeNaming(String code) {
        MetricResult metric = new MetricResult();
        List<Issue> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        
        // 分析类名
        Matcher classMatcher = CLASS_DEF.matcher(code);
        while (classMatcher.find()) {
            String className = classMatcher.group(2);
            if (!isValidClassName(className)) {
                issues.add(createIssue(
                    "类名 '" + className + "' 不符合驼峰命名规范",
                    "warning",
                    "类名应该以大写字母开头，使用驼峰命名法"
                ));
            }
        }
        
        // 分析方法名
        Matcher methodMatcher = METHOD_DEF.matcher(code);
        while (methodMatcher.find()) {
            String methodName = methodMatcher.group(2);
            if (!isValidMethodName(methodName)) {
                issues.add(createIssue(
                    "方法名 '" + methodName + "' 不符合驼峰命名规范",
                    "warning",
                    "方法名应该以小写字母开头，使用驼峰命名法"
                ));
            }
        }
        
        // 分析字段名
        Matcher fieldMatcher = FIELD_DEF.matcher(code);
        while (fieldMatcher.find()) {
            String fieldName = fieldMatcher.group(2);
            if (!isValidFieldName(fieldName)) {
                issues.add(createIssue(
                    "字段名 '" + fieldName + "' 不符合命名规范",
                    "warning",
                    "字段名应该以小写字母开头，使用驼峰命名法"
                ));
            }
        }
        
        // 计算得分
        int score = 100;
        score -= issues.size() * 10;  // 每个命名问题扣10分
        metric.setScore(Math.max(0, score));
        metric.setIssues(issues);
        
        // 添加建议
        if (!issues.isEmpty()) {
            suggestions.add("建议遵循Java命名规范：类名首字母大写，方法名和字段名首字母小写，都使用驼峰命名法");
            metric.setSuggestions(suggestions);
        }
        
        return metric;
    }

    private boolean isValidClassName(String name) {
        return name.matches("^[A-Z][a-zA-Z0-9]*$");
    }

    private boolean isValidMethodName(String name) {
        return name.matches("^[a-z][a-zA-Z0-9]*$");
    }

    private boolean isValidFieldName(String name) {
        return name.matches("^[a-z][a-zA-Z0-9]*$") && !name.matches(".*[A-Z]{2,}.*");
    }
}