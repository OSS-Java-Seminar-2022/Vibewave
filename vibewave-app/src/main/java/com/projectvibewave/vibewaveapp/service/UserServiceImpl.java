package com.projectvibewave.vibewaveapp.service;

import com.google.common.collect.Sets;
import com.projectvibewave.vibewaveapp.dto.EmailConfirmation;
import com.projectvibewave.vibewaveapp.enums.ConfirmationTokenStatus;
import com.projectvibewave.vibewaveapp.dto.UserSignUpDto;
import com.projectvibewave.vibewaveapp.entity.ConfirmationToken;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.repository.ConfirmationTokenRepository;
import com.projectvibewave.vibewaveapp.repository.RoleRepository;
import com.projectvibewave.vibewaveapp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.servlet.ServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final static String DEFAULT_ROLE_NAME = "ROLE_BASIC";
    private final static int TOKEN_EXPIRATION_TIME_MINUTES = 15;
    private final static String USER_NOT_FOUND_MSG = "User %s not found";
    private final static String EMAIL_CONFIRMATION_SUBJECT = "VibeWave - Confirm Your E-Mail";
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final EmailService emailService;
    private final ServletRequest servletRequest;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, username))
        );
    }

    @Override
    public boolean trySignUp(UserSignUpDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        if (!userDto.getPassword().equals(userDto.getRepeatedPassword())) {
            bindingResult.rejectValue("repeatedPassword", "error.user", "Passwords do not match");
            return false;
        }

        var usernameExists = userRepository.existsByUsername(userDto.getUsername());
        if (usernameExists) {
            bindingResult.rejectValue("username", "error.user", "Username already exists");
            return false;
        }

        var emailExists = userRepository.existsByEmail(userDto.getEmail());
        if (emailExists) {
            bindingResult.rejectValue("email", "error.user", "E-Mail already exists");
            return false;
        }

        var defaultRole = roleRepository.findByName(DEFAULT_ROLE_NAME).orElseThrow(() ->
                new RuntimeException("Default role was not found in database")
        );

        var user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .email(userDto.getEmail())
                .roles(Sets.newHashSet(defaultRole))
                .build();

        userRepository.save(user);

        createConfirmationTokenAndSendEmail(user);

        return true;
    }

    @Override
    public void createConfirmationTokenAndSendEmail(User user) {
        String token = UUID.randomUUID().toString();
        var confirmationToken = ConfirmationToken.builder()
                .user(user)
                .token(token)
                .build();

        confirmationTokenRepository.save(confirmationToken);

        var templateModel = new HashMap<String, Object>();
        var port = servletRequest.getServerPort();
        var hostName = servletRequest.getServerName();
        var confirmationLink = "http://" + hostName + ":" + port + "/user/confirm?token=" + token;
        templateModel.put("confirmationLink", confirmationLink);
        templateModel.put("username", user.getUsername());

        emailService.sendHtml(user.getEmail(), EMAIL_CONFIRMATION_SUBJECT, templateModel);
    }

    @Override
    public ConfirmationTokenStatus tryConfirmEmail(String token) {
        var confirmationToken = confirmationTokenRepository.findByToken(token);

        if (confirmationToken.isEmpty()) {
            return ConfirmationTokenStatus.NOT_FOUND;
        }

        var foundConfirmationToken = confirmationToken.get();
        var user = foundConfirmationToken.getUser();

        if (user.isEnabled()) {
            return ConfirmationTokenStatus.ALREADY_CONFIRMED;
        }

        if (isConfirmationTokenExpired(foundConfirmationToken)) {
            return ConfirmationTokenStatus.EXPIRED;
        }

        foundConfirmationToken.setConfirmedAt(LocalDateTime.now());
        confirmationTokenRepository.save(foundConfirmationToken);

        user.setEnabled(true);
        userRepository.save(user);

        return ConfirmationTokenStatus.SUCCESSFULLY_CONFIRMED;
    }

    @Transactional
    @Override
    public boolean reSendConfirmationToken(EmailConfirmation emailConfirmation, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        var user = userRepository.findByEmail(emailConfirmation.getEmail());

        if (user.isEmpty()) {
            bindingResult.rejectValue("email", "error.emailConfirmation", "No user found with that E-Mail");
            return false;
        }

        var foundUser = user.get();
        if (foundUser.isEnabled()) {
            bindingResult.rejectValue("email", "error.emailConfirmation", "Account with that E-Mail has already been confirmed");
            return false;
        }

        confirmationTokenRepository.removeAllByUser(foundUser);

        createConfirmationTokenAndSendEmail(foundUser);
        return true;
    }

    private boolean isConfirmationTokenExpired(ConfirmationToken confirmationToken) {
        return LocalDateTime.now().isAfter(confirmationToken.getCreatedAt().plusMinutes(TOKEN_EXPIRATION_TIME_MINUTES));
    }
}
