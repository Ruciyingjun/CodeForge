package com.ruoyi.system.domain;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
public class CodeAnalyzeRequest {
    /** 编程语言 */
    @NotBlank(message = "编程语言不能为空")
    private String language;
    
    /** 代码内容 */
    @NotBlank(message = "代码内容不能为空")
    private String code;
    
    /** 分析选项 */
    private Map<String, Object> options;
} 