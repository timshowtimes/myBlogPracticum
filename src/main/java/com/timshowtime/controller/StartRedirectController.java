package com.timshowtime.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StartRedirectController {

    @GetMapping("/")
    public String redirect() {
        return "redirect:/posts";
    }
}
