package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.dto.UserSignUpDto;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String signUp(Model model) {
        logger.info("Accessed Home View");
        var user = new UserSignUpDto();
        model.addAttribute("user", user);
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signUp(@ModelAttribute("user") UserSignUpDto request) {
        logger.info("trying to sign up a user....");
        userService.signUp(request);
        return "redirect:/";
    }
}
