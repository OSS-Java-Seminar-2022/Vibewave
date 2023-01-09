package com.projectvibewave.vibewaveapp.service;

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
    private final static int REQUIRED_TOTAL_PLAYS = 5;
    private UserRepository userRepository;
    private VerificationRequestRepository verificationRequestRepository;

    @Override
    public boolean setVerificationRequestModel(User authenticatedUser, Model model) {
        if (!model.containsAttribute("verificationRequest")) {
            var verificationRequestPostDto = new VerificationRequestPostDto();
            model.addAttribute("verificationRequest", verificationRequestPostDto);
        }

        var sentRequests = verificationRequestRepository.findAllByUserOrderByStatusAscDateAppliedDesc(authenticatedUser);
        var isRequestCurrentlyPending = sentRequests.stream().anyMatch(sr -> sr.getStatus() == VerificationRequestStatus.PENDING);
        model.addAttribute("sentRequests", sentRequests);
        model.addAttribute("isRequestCurrentlyPending", isRequestCurrentlyPending);
        model.addAttribute("hasUserMetRequiredConditions", hasUserMetRequiredConditions(authenticatedUser));
        return true;
    }

    @Override
    public boolean trySendVerificationRequest(User authenticatedUser, VerificationRequestPostDto verificationRequestPostDto, BindingResult bindingResult, Model model) {
        setVerificationRequestModel(authenticatedUser, model);

        if (!hasUserMetRequiredConditions(authenticatedUser) || bindingResult.hasErrors()) {
            return false;
        }

        var pendingRequests =
                verificationRequestRepository.findAllByStatus(VerificationRequestStatus.PENDING);

        if (authenticatedUser.isVerified() || pendingRequests.size() > 0) {
            return false;
        }

        var verificationRequest = VerificationRequest.builder()
                .message(verificationRequestPostDto.getMessage())
                .user(authenticatedUser)
                .build();

        verificationRequestRepository.save(verificationRequest);
        return true;
    }

    private boolean hasUserMetRequiredConditions(User authenticatedUser) {
        var userTotalPlays = userRepository.getTotalPlaysByUser(authenticatedUser.getId());
        return userTotalPlays >= REQUIRED_TOTAL_PLAYS;
    }
}
