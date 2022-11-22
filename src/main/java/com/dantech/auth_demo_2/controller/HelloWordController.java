package com.dantech.auth_demo_2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWordController {
    @GetMapping(value = "/hello")
    public String firstPage(){
        return "Hello word";
    }

}
