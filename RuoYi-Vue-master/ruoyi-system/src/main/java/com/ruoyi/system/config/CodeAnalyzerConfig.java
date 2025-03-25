package com.ruoyi.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "code.analyzer")
public class CodeAnalyzerConfig {
    /** 最大行长度 */
    private int maxLineLength;
    
    /** 是否启用严格模式 */
    private boolean strictMode;
    
    /** 排除的文件模式 */
    private String[] excludePatterns;
    
    /** 规则配置 */
    private Map<String, RuleConfig> rules;
    
    @Data
    public static class RuleConfig {
        /** 是否启用 */
        private boolean enabled;
        
        /** 严重程度 */
        private String severity;
        
        /** 规则参数 */
        private Map<String, Object> params;
    }
} 