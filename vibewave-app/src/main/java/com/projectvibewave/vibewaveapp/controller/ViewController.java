package com.projectvibewave.vibewaveapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ViewController {
    private final Logger logger = LoggerFactory.getLogger(ViewController.class);

    @GetMapping("/")
    public String homeView() {
        logger.info("Accessed Home View");
        return "home";
    }
}
