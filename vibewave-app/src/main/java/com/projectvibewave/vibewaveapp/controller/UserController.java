package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.dto.UserSignUpDto;
import com.projectvibewave.vibewaveapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/login")
    public String logIn(Model model) {
        logger.info("Accessed Log In Page");
        return "user/login";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/signup")
    public String signUp(Model model) {
        logger.info("Accessed Sign Up Page");
        var user = new UserSignUpDto();
        model.addAttribute("user", user);
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signUp(@Valid @ModelAttribute("user") UserSignUpDto user, BindingResult bindingResult, Model model) {
        logger.info("trying to sign up a user....");
        var isSuccessful = userService.trySignUp(user, bindingResult);

        if (!isSuccessful) {
            return "user/signup";
        }

        return "redirect:/?signup";
    }
}
