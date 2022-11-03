package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.entity.ConfirmationToken;
import com.projectvibewave.vibewaveapp.entity.User;

import java.util.Optional;

public interface ConfirmationTokenService {
    void save(ConfirmationToken token);

    Optional<ConfirmationToken> findByToken(String token);
    void removeAllByUser(User user);
}
