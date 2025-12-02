package com.example.buzzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String helloWorld() {
        return "Hello World!";
    }
    
    @GetMapping("/")
    public ModelAndView index() {
        return new ModelAndView("redirect:/word-guess-game.html");
    }

}