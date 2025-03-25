package com.ruoyi.system.analyzer.utils;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.List;

public class CodeAnalyzerUtils {
    
    // 代码复杂度评分标准
    private static final int MAX_CYCLOMATIC_COMPLEXITY = 15;  // 圈复杂度上限
    private static final int MAX_NESTING_DEPTH = 4;          // 最大嵌套深度
    private static final int MAX_METHOD_LENGTH = 50;         // 方法长度上限
    
    // 命名规范正则表达式
    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern PASCAL_CASE_PATTERN = Pattern.compile("^[A-Z][a-zA-Z0-9]*$");
    private static final Pattern CONSTANT_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");
    
    public static int calculateComplexityScore(int cyclomaticComplexity, 
                                             int maxNestingDepth,
                                             Map<String, Integer> methodLengths) {
        int score = 100;
        
        // 根据圈复杂度评分
        if (cyclomaticComplexity > MAX_CYCLOMATIC_COMPLEXITY) {
            score -= 30;
        } else if (cyclomaticComplexity > 10) {
            score -= 20;
        } else if (cyclomaticComplexity > 5) {
            score -= 10;
        }
        
        // 根据嵌套深度评分
        if (maxNestingDepth > MAX_NESTING_DEPTH) {
            score -= 20;
        } else if (maxNestingDepth > 3) {
            score -= 10;
        }
        
        // 根据方法长度评分
        for (int length : methodLengths.values()) {
            if (length > MAX_METHOD_LENGTH) {
                score -= 15;
            } else if (length > 30) {
                score -= 10;
            } else if (length > 20) {
                score -= 5;
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    public static int getIndentationLevel(String line) {
        int indent = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                indent++;
            } else if (c == '\t') {
                indent += 4; // 将制表符视为4个空格
            } else {
                break;
            }
        }
        return indent;
    }
    
    public static boolean isValidCamelCase(String name) {
        return CAMEL_CASE_PATTERN.matcher(name).matches();
    }
    
    public static boolean isValidPascalCase(String name) {
        return PASCAL_CASE_PATTERN.matcher(name).matches();
    }
    
    public static boolean isValidConstantName(String name) {
        return CONSTANT_PATTERN.matcher(name).matches();
    }
    
    public static int calculateNamingScore(List<String> violations) {
        int baseScore = 100;
        int deductionPerViolation = 5;
        return Math.max(0, baseScore - (violations.size() * deductionPerViolation));
    }
    
    public static int calculateStyleScore(List<String> violations) {
        int baseScore = 100;
        int deductionPerViolation = 3;
        return Math.max(0, baseScore - (violations.size() * deductionPerViolation));
    }
    
    public static String formatMessage(String message, int lineNumber) {
        return String.format("第%d行: %s", lineNumber, message);
    }
    
    public static String getSeverityLevel(int score) {
        if (score >= 80) return "info";
        if (score >= 60) return "warning";
        return "error";
    }
    
    // 检查代码中的注释比例
    public static double calculateCommentRatio(String code) {
        String[] lines = code.split("\n");
        int commentLines = 0;
        int codeLines = 0;
        
        boolean inMultiLineComment = false;
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.isEmpty()) continue;
            
            if (inMultiLineComment) {
                commentLines++;
                if (line.contains("*/")) {
                    inMultiLineComment = false;
                }
            } else {
                if (line.startsWith("//")) {
                    commentLines++;
                } else if (line.startsWith("/*")) {
                    commentLines++;
                    inMultiLineComment = true;
                } else if (line.contains("//")) {
                    commentLines++;
                    codeLines++;
                } else {
                    codeLines++;
                }
            }
        }
        
        return codeLines == 0 ? 0 : (double) commentLines / codeLines;
    }
    
    // 检查空行使用是否合理
    public static boolean hasProperSpacing(String code) {
        String[] lines = code.split("\n");
        int consecutiveEmptyLines = 0;
        
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                consecutiveEmptyLines++;
                if (consecutiveEmptyLines > 2) {
                    return false;
                }
            } else {
                consecutiveEmptyLines = 0;
            }
        }
        
        return true;
    }
    
    // 检查行长度
    public static boolean isLineTooLong(String line, int maxLength) {
        return line.length() > maxLength;
    }
    
    // 检查代码重复
    public static boolean hasCodeDuplication(String code, int minLineCount) {
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length - minLineCount; i++) {
            String block = String.join("\n", java.util.Arrays.copyOfRange(lines, i, i + minLineCount));
            int count = 0;
            int pos = code.indexOf(block);
            while (pos != -1) {
                count++;
                pos = code.indexOf(block, pos + 1);
            }
            if (count > 1) {
                return true;
            }
        }
        return false;
    }
} 