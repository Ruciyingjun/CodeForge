package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 原始版本：安全的登录与文件获取逻辑
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        // 使用参数化查询，防止 SQL 注入
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        return jdbcTemplate.queryForObject(sql, String.class, username, password);
    }

    @GetMapping("/file")
    public File getUserFile(@RequestParam String filename) {
        // 安全的路径拼接（假设已有基础校验）
        return new File("/var/www/user_files/" + filename);
    }
}
