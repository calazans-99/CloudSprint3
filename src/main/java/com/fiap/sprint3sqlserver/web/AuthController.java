// src/main/java/com/fiap/sprint3sqlserver/web/AuthController.java
package com.fiap.sprint3sqlserver.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        // Thymeleaf vai procurar templates/login.html
        return "login";
    }
}
