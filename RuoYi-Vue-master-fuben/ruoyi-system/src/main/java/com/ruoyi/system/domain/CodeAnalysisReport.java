package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Collections;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class CodeAnalysisReport implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 总体评分 */
    private Integer score;
    
    /** 分析时间 */
    private Date analyzeTime;
    
    /** 各项指标得分和问题 */
    private Map<String, MetricResult> metrics;
    
    /** 文件信息 */
    private FileInfo fileInfo;
    
    /** 分析耗时(毫秒) */
    private long duration;
    
    @Data
    public static class MetricResult implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** 该维度得分 */
        private Integer score;
        
        /** 发现的问题列表 */
        private List<Issue> issues;
        
        /** 改进建议列表 */
        private List<String> suggestions;
        
        /** 详细分析数据 */
        private Map<String, Object> details;
        
        /** 是否通过检查 */
        private boolean passed;
    }
    
    @Data
    public static class Issue implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** 问题描述 */
        private String message;
        
        /** 严重程度(error/warning/info) */
        private String severity;
        
        /** 改进建议 */
        private String suggestion;
        
        /** 问题代码片段 */
        private String code;
        
        /** 问题所在行号 */
        private int lineNumber;
        
        /** 问题所在列号 */
        private int columnNumber;
        
        /** 问题类型 */
        private String type;
        
        /** 规则ID */
        private String ruleId;
    }
    
    @Data
    public static class FileInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** 文件名 */
        private String fileName;
        
        /** 文件大小(字节) */
        private long fileSize;
        
        /** 总行数 */
        private int totalLines;
        
        /** 代码行数 */
        private int codeLines;
        
        /** 注释行数 */
        private int commentLines;
        
        /** 空行数 */
        private int emptyLines;
        
        /** 文件类型 */
        private String fileType;
        
        /** 文件MD5 */
        private String fileMd5;
    }
    
    /** 获取指定维度的分析结果 */
    public MetricResult getMetric(String metricName) {
        return metrics != null ? metrics.get(metricName) : null;
    }
    
    /** 判断是否通过所有检查 */
    public boolean isPassed() {
        if (metrics == null) {
            return false;
        }
        return metrics.values().stream()
                .allMatch(MetricResult::isPassed);
    }
    
    /** 获取所有严重(error)级别的问题 */
    public List<Issue> getErrorIssues() {
        return getAllIssues().stream()
                .filter(issue -> "error".equals(issue.getSeverity()))
                .collect(Collectors.toList());
    }
    
    /** 获取所有警告(warning)级别的问题 */
    public List<Issue> getWarningIssues() {
        return getAllIssues().stream()
                .filter(issue -> "warning".equals(issue.getSeverity()))
                .collect(Collectors.toList());
    }
    
    /** 获取所有提示(info)级别的问题 */
    public List<Issue> getInfoIssues() {
        return getAllIssues().stream()
                .filter(issue -> "info".equals(issue.getSeverity()))
                .collect(Collectors.toList());
    }
    
    /** 获取所有问题 */
    public List<Issue> getAllIssues() {
        if (metrics == null) {
            return Collections.emptyList();
        }
        return metrics.values().stream()
                .filter(metric -> metric.getIssues() != null)
                .flatMap(metric -> metric.getIssues().stream())
                .collect(Collectors.toList());
    }
    
    /** 获取所有建议 */
    public List<String> getAllSuggestions() {
        if (metrics == null) {
            return Collections.emptyList();
        }
        return metrics.values().stream()
                .filter(metric -> metric.getSuggestions() != null)
                .flatMap(metric -> metric.getSuggestions().stream())
                .collect(Collectors.toList());
    }
} 