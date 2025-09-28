// src/main/java/com/fiap/sprint3sqlserver/web/HomeController.java
package com.fiap.sprint3sqlserver.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/index"})
    public String index() {
        // abre static/index.html (UI de testes)
        return "forward:/index.html";
    }
}
