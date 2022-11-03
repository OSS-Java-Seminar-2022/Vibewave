package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.EmailConfirmation;
import com.projectvibewave.vibewaveapp.enums.ConfirmationTokenStatus;
import com.projectvibewave.vibewaveapp.dto.UserSignUpDto;
import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;

public interface UserService extends UserDetailsService {
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    boolean trySignUp(UserSignUpDto userDto, BindingResult bindingResult);

    void createConfirmationTokenAndSendEmail(User user);

    ConfirmationTokenStatus tryConfirmEmail(String token);

    boolean reSendConfirmationToken(EmailConfirmation emailConfirmation, BindingResult bindingResult);
}
