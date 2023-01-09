package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.VerificationRequestPostDto;
import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

public interface VerificationService {
    boolean setVerificationRequestModel(User authenticatedUser, Model model);

    boolean trySendVerificationRequest(User authenticatedUser, VerificationRequestPostDto verificationRequestPostDto, BindingResult bindingResult, Model model);
}
