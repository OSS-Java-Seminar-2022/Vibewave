package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.entity.ConfirmationToken;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.repository.ConfirmationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;

}
