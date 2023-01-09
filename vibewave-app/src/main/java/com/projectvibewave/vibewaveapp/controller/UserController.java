package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.dto.UserSettingsDto;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Controller
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @GetMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public String getUserSettings(Authentication authentication, Model model) {
        logger.info("Accessed User Settings Page");

        var authenticatedUser = (User) authentication.getPrincipal();
        var userSettings = userService.getAuthenticatedUserSettings(authenticatedUser);
        model.addAttribute("settings", userSettings);
        return "user/settings";
    }

    @PostMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public String updateUserSettings(Authentication authentication,
                                     @Valid @ModelAttribute("settings") UserSettingsDto userSettingsDto,
                                     BindingResult bindingResult, Model model) throws IOException  {
        logger.info("Trying to update user settings....");

        var authenticatedUser = (User) authentication.getPrincipal();
        var isSuccessful = userService.tryUpdateUserSettings(authenticatedUser, userSettingsDto, bindingResult);

        if (!isSuccessful) {
            return "user/settings";
        }

        return "redirect:/user/settings/?updated";
    }

    @GetMapping("/{userId}")
    public String getUserProfileView(Authentication authentication, Model model, @PathVariable @NotNull Long userId) {
        logger.info("Accessed User Profile Page");

        var authenticatedUser = authentication != null ? (User)authentication.getPrincipal() : null;

        var exists = userService.setUserByIdModelView(model, authenticatedUser, userId);

        if (!exists) {
            return "redirect:/";
        }

        return "user/profile";
    }

    @PostMapping("/follow/{userId}")
    @PreAuthorize("isAuthenticated()")
    public String tryFollowOrUnfollowUser(Authentication authentication, @PathVariable @NotNull Long userId) {
        logger.info("trying to register a follow");

        var authenticatedUser = (User)authentication.getPrincipal();

        var isSuccessful = userService.tryFollowOrUnfollowUser(authenticatedUser, userId);

        if (!isSuccessful) {
            return "redirect:/";
        }

        return "redirect:/user/" + userId;
    }
}
