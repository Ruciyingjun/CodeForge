package com.ruoyi.common.core.domain;

import com.ruoyi.common.annotation.Excel;
import lombok.Data;

@Data
public class TableStructure {
    @Excel(name = "字段名")
    private String columnName;

    @Excel(name = "数据类型")
    private String dataType;

    @Excel(name = "是否为空")
    private String nullable;

    @Excel(name = "默认值")
    private String defaultValue;

    @Excel(name = "备注")
    private String comment;
}