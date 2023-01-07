package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.dto.VerificationRequestPostDto;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.service.VerificationService;
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

@Controller
@AllArgsConstructor
@RequestMapping("/verification")
public class VerificationController {
    private final Logger logger = LoggerFactory.getLogger(VerificationController.class);
    private final VerificationService verificationService;

    @GetMapping("/request")
    @PreAuthorize("isAuthenticated()")
    public String getVerificationRequestView(Authentication authentication, Model model) {
        logger.info("Accessed Verification Request Page");

        var authenticatedUser = (User) authentication.getPrincipal();
        verificationService.setVerificationRequestModel(authenticatedUser, model);

        return "user/verification-request";
    }

    @PostMapping("/request")
    @PreAuthorize("isAuthenticated() and !#authentication.principal.verified")
    public String trySendVerificationRequest(Authentication authentication,
                                            @Valid @ModelAttribute("verificationRequest") VerificationRequestPostDto verificationRequestPostDto,
                                            BindingResult bindingResult,
                                            Model model) {
        logger.info("Accessed Verification Request Page");

        var authenticatedUser = (User) authentication.getPrincipal();

        if (!verificationService.hasUserMetRequiredConditions(authenticatedUser)) {
            return "redirect:/verification/request";
        }

        var isSuccessful = verificationService.trySendVerificationRequest(authenticatedUser, verificationRequestPostDto, bindingResult, model);

        if (!isSuccessful) {
            return "user/verification-request";
        }

        return "redirect:/verification/request";
    }
}
