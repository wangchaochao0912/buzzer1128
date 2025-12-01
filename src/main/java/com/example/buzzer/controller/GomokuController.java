package com.example.buzzer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GomokuController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
