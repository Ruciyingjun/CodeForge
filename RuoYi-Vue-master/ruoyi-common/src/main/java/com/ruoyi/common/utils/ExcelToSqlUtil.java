package com.ruoyi.common.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.ruoyi.common.core.domain.TableStructure;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.io.InputStream;

public class ExcelToSqlUtil {
    // 添加缓存，避免重复计算数据类型
    private static final Map<String, String> MYSQL_TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> ORACLE_TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> SQLSERVER_TYPE_CACHE = new ConcurrentHashMap<>();
    
    public static String convertToSql(InputStream inputStream, String tableName, String dbType) throws Exception {
        long startTime = System.currentTimeMillis();
        List<TableStructure> tableStructures = new ArrayList<>(32);
        StringBuilder sql = new StringBuilder(2048);
        
        try {
            EasyExcel.read(inputStream, new AnalysisEventListener<Map<Integer, String>>() {
                private final int batchSize = 1000;
                private final List<TableStructure> batch = new ArrayList<>(batchSize);
                private int totalRows = 0;
                
                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    totalRows++;
                    if (data == null || data.isEmpty() || context.readRowHolder().getRowIndex() == 0) {
                        return;
                    }
                    
                    TableStructure structure = new TableStructure();
                    structure.setColumnName(StringUtils.trimToEmpty(data.get(0)));
                    structure.setDataType(StringUtils.trimToEmpty(data.get(1)));
                    structure.setNullable(StringUtils.trimToEmpty(data.get(2)));
                    structure.setDefaultValue(StringUtils.trimToEmpty(data.get(3)));
                    structure.setComment(StringUtils.trimToEmpty(data.get(4)));
                    
                    if (!structure.getColumnName().isEmpty() && !structure.getDataType().isEmpty()) {
                        batch.add(structure);
                    }
                    
                    if (batch.size() >= batchSize) {
                        tableStructures.addAll(batch);
                        batch.clear();
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    if (!batch.isEmpty()) {
                        tableStructures.addAll(batch);
                    }
                    if (tableStructures.isEmpty()) {
                        throw new RuntimeException("Excel文件中没有有效的数据");
                    }
                    
                    long readTime = System.currentTimeMillis();
                    System.out.println("Excel读取完成，总行数：" + totalRows + "，有效数据：" + tableStructures.size() + 
                                     "，耗时：" + (readTime - startTime) + "ms");
                    
                    generateCreateTableSql(sql, tableStructures, tableName, dbType);
                    
                    long endTime = System.currentTimeMillis();
                    System.out.println("SQL生成完成，耗时：" + (endTime - readTime) + "ms");
                }
            })
            .autoCloseStream(true)
            .sheet()
            .doRead();
            
            return sql.toString();
            
        } catch (Exception e) {
            System.err.println("转换失败：" + e.getMessage());
            throw e;
        }
    }
    
    private static String getMySQLDataType(String dataType) {
        return MYSQL_TYPE_CACHE.computeIfAbsent(dataType.toLowerCase(), key -> {
            if (key.contains("varchar")) return key;
            switch (key) {
                case "int": return "int(11)";
                case "datetime": return "datetime";
                case "decimal": return "decimal(10,2)";
                case "tinyint": return "tinyint(1)";
                default: return key;
            }
        });
    }
    
    // 同样优化 Oracle 和 SQLServer 的数据类型处理方法
    private static String getOracleDataType(String dataType) {
        dataType = dataType.toLowerCase();
        if (dataType.contains("varchar")) {
            return dataType.replace("varchar", "varchar2");
        } else if (dataType.equals("int")) {
            return "number(11)";
        } else if (dataType.equals("datetime")) {
            return "date";
        } else if (dataType.equals("decimal")) {
            return "number(10,2)";
        } else if (dataType.equals("tinyint")) {
            return "number(1)";
        }
        return dataType;
    }
    
    private static String getSqlServerDataType(String dataType) {
        dataType = dataType.toLowerCase();
        if (dataType.contains("varchar")) {
            return dataType.replace("varchar", "nvarchar");
        } else if (dataType.equals("int")) {
            return "int";
        } else if (dataType.equals("datetime")) {
            return "datetime";
        } else if (dataType.equals("decimal")) {
            return "decimal(10,2)";
        } else if (dataType.equals("tinyint")) {
            return "tinyint";
        }
        return dataType;
    }
    
    private static void generateCreateTableSql(StringBuilder sql, List<TableStructure> structures, 
                                             String tableName, String dbType) {
        sql.append("CREATE TABLE ").append(tableName).append(" (\n");
        
        for (int i = 0; i < structures.size(); i++) {
            TableStructure structure = structures.get(i);
            sql.append("    ").append(structure.getColumnName()).append(" ");
            
            // 使用缓存处理数据类型
            switch (dbType.toLowerCase()) {
                case "mysql":
                    sql.append(getMySQLDataType(structure.getDataType()));
                    break;
                case "oracle":
                    sql.append(getOracleDataType(structure.getDataType()));
                    break;
                case "sqlserver":
                    sql.append(getSqlServerDataType(structure.getDataType()));
                    break;
            }
            
            if ("否".equals(structure.getNullable())) {
                sql.append(" NOT NULL");
            }
            
            String defaultValue = structure.getDefaultValue();
            if (defaultValue != null && !defaultValue.isEmpty()) {
                appendDefaultValue(sql, defaultValue, structure.getDataType());
            }
            
            String comment = structure.getComment();
            if (comment != null && !comment.isEmpty() && "mysql".equalsIgnoreCase(dbType)) {
                sql.append(" COMMENT '").append(comment).append("'");
            }
            
            if (i < structures.size() - 1) {
                sql.append(",");
            }
            sql.append("\n");
        }
        
        sql.append(")");
        
        if ("mysql".equalsIgnoreCase(dbType)) {
            sql.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='").append(tableName).append("'");
        }
        
        sql.append(";\n");
    }

    private static void appendDefaultValue(StringBuilder sql, String defaultValue, String dataType) {
        sql.append(" DEFAULT ");
        if (defaultValue.equalsIgnoreCase("CURRENT_TIMESTAMP")) {
            sql.append("CURRENT_TIMESTAMP");
        } else if (dataType.toLowerCase().contains("varchar") || 
                  dataType.toLowerCase().contains("char") || 
                  dataType.toLowerCase().contains("text")) {
            sql.append("'").append(defaultValue).append("'");
        } else {
            sql.append(defaultValue);
        }
    }
}