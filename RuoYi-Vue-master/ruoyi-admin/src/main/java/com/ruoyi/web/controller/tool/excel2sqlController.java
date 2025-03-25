package com.ruoyi.web.controller.tool;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ExcelToSqlUtil;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.sun.xml.bind.v2.TODO;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.domain.TableStructure;
import com.ruoyi.common.annotation.Anonymous;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/tool/excel2sql")
@Anonymous
public class excel2sqlController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(excel2sqlController.class);

    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file, 
                           @RequestParam("tableName") String tableName,
                           @RequestParam("dbType") String dbType) {
        try {
            log.info("开始处理Excel转SQL请求，文件名：{}，表名：{}，数据库类型：{}", 
                    file.getOriginalFilename(), tableName, dbType);
            long startTime = System.currentTimeMillis();

            if (file.isEmpty()) {
                return AjaxResult.error("请选择文件");
            }

            String sql = ExcelToSqlUtil.convertToSql(file.getInputStream(), tableName, dbType);
            
            long endTime = System.currentTimeMillis();
            log.info("Excel转SQL完成，耗时：{}ms", (endTime - startTime));
            
            return AjaxResult.success(sql);
        } catch (Exception e) {
            log.error("Excel转SQL失败", e);
            return AjaxResult.error("转换失败：" + e.getMessage());
        }
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            // 假设模板文件路径为 classpath 下的 template.xlsx
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("template.xlsx");
            if (inputStream == null) {
                log.error("模板文件 template.xlsx 未找到");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=template.xlsx");

            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("下载模板失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


}