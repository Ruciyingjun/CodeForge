package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询用户信息（安全版本）
     * 使用参数化查询，防止 SQL 注入
     */
    @GetMapping("/user/safe")
    public String getUserSafe(@RequestParam String name) {
        // 安全的参数化查询
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE name = ?", String.class, name);
    }
    
    // ... 其他业务代码
}
