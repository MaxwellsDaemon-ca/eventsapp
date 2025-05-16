package com.shadsluiter.eventsapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * This controller handles the home page
 */
@Controller
@RequestMapping("/")
public class HomeController {

    
    @RequestMapping("/")
    public String home() {
        return "/home";
    }
    
}
