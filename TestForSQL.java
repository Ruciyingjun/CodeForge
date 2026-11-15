
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

    
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        
        String sql = "SELECT role FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
        
        
        String role = jdbcTemplate.queryForObject(sql, String.class);
        return role.toUpperCase(); 
    }

    @GetMapping("/file")
    public File getUserFile(@RequestParam String filename) {
        
        String basePath = "/var/www/user_files/";
        
        
        return new File(basePath + filename);
    }
}
