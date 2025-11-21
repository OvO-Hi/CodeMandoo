package com.example.record.application;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class FConnection {
    @RequestMapping("/test")
    public String Test(){

        return "connection test";
    }
}

