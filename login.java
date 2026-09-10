package com.example.checkout.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, @RequestParam String password, Model model) {
        // Mock authentication check
        if ("buyer@example.com".equals(email) && "password123".equals(password)) {
            return "redirect:/store";
        }
        
        model.addAttribute("error", "Invalid email or password. Please try again.");
        return "login";
    }
}