package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.AdminEditUserDto;
import com.projectvibewave.vibewaveapp.dto.VerificationRequestPostDto;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.entity.VerificationRequest;
import com.projectvibewave.vibewaveapp.enums.VerificationRequestStatus;
import com.projectvibewave.vibewaveapp.repository.UserRepository;
import com.projectvibewave.vibewaveapp.repository.VerificationRequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Service
@AllArgsConstructor
public class VerificationServiceImpl implements VerificationService {
    private UserRepository userRepository;
    private VerificationRequestRepository verificationRequestRepository;

    @Override
    public boolean setVerificationRequestModel(User authenticatedUser, Model model) {
        if (!model.containsAttribute("verificationRequest")) {
            var verificationRequestPostDto = new VerificationRequestPostDto();
            model.addAttribute("verificationRequest", verificationRequestPostDto);
        }

        var sentRequest = verificationRequestRepository.findAllByUserOrderByStatusAscDateAppliedDesc(authenticatedUser);
        var isRequestCurrentlyPending = sentRequest.stream().anyMatch(sr -> sr.getStatus() == VerificationRequestStatus.PENDING);
        model.addAttribute("sentRequests", sentRequest);
        model.addAttribute("isRequestCurrentlyPending", isRequestCurrentlyPending);
        return true;
    }

    @Override
    public boolean trySendVerificationRequest(User authenticatedUser, VerificationRequestPostDto verificationRequestPostDto, BindingResult bindingResult, Model model) {
        setVerificationRequestModel(authenticatedUser, model);

        if (bindingResult.hasErrors()) {
            return false;
        }

        var approvedRequests =
                verificationRequestRepository.findAllByStatus(VerificationRequestStatus.APPROVED);
        var pendingRequests =
                verificationRequestRepository.findAllByStatus(VerificationRequestStatus.PENDING);

        if (approvedRequests.size() > 0 || pendingRequests.size() > 0) {
            return false;
        }

        var verificationRequest = VerificationRequest.builder()
                .message(verificationRequestPostDto.getMessage())
                .user(authenticatedUser)
                .build();

        verificationRequestRepository.save(verificationRequest);
        return true;
    }
}
