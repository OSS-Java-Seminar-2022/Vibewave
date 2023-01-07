package com.projectvibewave.vibewaveapp.service;

import com.google.common.collect.Sets;
import com.projectvibewave.vibewaveapp.dto.EmailConfirmationDto;
import com.projectvibewave.vibewaveapp.dto.UserSettingsDto;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.servlet.ServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;

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
    private final FileService fileService;
    private final ServletRequest servletRequest;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
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
        var confirmationLink = "http://" + hostName + ":" + port + "/auth/confirm?token=" + token;
        templateModel.put("confirmationLink", confirmationLink);
        templateModel.put("username", user.getUsername());

        emailService.sendHtml(user.getEmail(), EMAIL_CONFIRMATION_SUBJECT, templateModel, EmailService.ACCOUNT_CONFIRMATION_TEMPLATE);
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

    @Override
    @Transactional
    public boolean reSendConfirmationToken(EmailConfirmationDto emailConfirmationDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        var user = userRepository.findByEmail(emailConfirmationDto.getEmail());

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

    @Override
    public boolean tryUpdateUserSettings(UserSettingsDto userSettingsDto, BindingResult bindingResult) {
        var shouldSetArtistNameToNull = "".equals(userSettingsDto.getArtistName());
        if (bindingResult.hasErrors() && !shouldSetArtistNameToNull) {
            return false;
        }

        var userToUpdate = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var artistNameExists = userRepository.existsByArtistName(userSettingsDto.getArtistName());
        var isArtistNameChanged = !Objects.equals(userSettingsDto.getArtistName(), userToUpdate.getArtistName());
        if (artistNameExists && isArtistNameChanged) {
            bindingResult.rejectValue("artistName", "error.user", "Artist name is already being used.");
            return false;
        }

        var file = userSettingsDto.getProfilePhoto();
        var contentType = file.getContentType();
        var size = file.getSize();
        var shouldUpdateProfilePhoto = size > 0;

        if (shouldUpdateProfilePhoto && (!FileService.ALLOWED_IMAGE_FILE_TYPES.contains(contentType) || size > FileService.TEN_MEGABYTES)) {
            bindingResult.rejectValue("profilePhoto", "error.settings",
                    "Please make sure the image is either jpeg or png, and the size is no bigger than 10MB.");
            return false;
        }

        if (shouldUpdateProfilePhoto) {
            var filename = fileService.save(file);
            userToUpdate.setProfilePhotoUrl(filename);
        }
        userToUpdate.setArtistName(shouldSetArtistNameToNull ? null : userSettingsDto.getArtistName());
        userToUpdate.setPrivate(userSettingsDto.isPrivate());
        userRepository.save(userToUpdate);

        return true;
    }

    @Override
    public UserSignUpDto getUserSignUpDto() {
        return new UserSignUpDto();
    }

    @Override
    public UserSettingsDto getAuthenticatedUserSettings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var username = auth.getName();
        var currentUser = loadUserByUsername(username);

        var userSettings = new UserSettingsDto();
        userSettings.setArtistName(currentUser.getArtistName());
        userSettings.setPrivate(currentUser.getPrivate());

        return userSettings;
    }

    @Override
    public boolean setUserByIdModelView(Model model, User authenticatedUser, Long userId) {
        var user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return false;
        }

        model.addAttribute("user", user);

        if (authenticatedUser != null) {
            var follower = userRepository.findById(authenticatedUser.getId()).orElse(null);
            var isFollowing = user.getFollowers().contains(follower);
            model.addAttribute("isFollowing", isFollowing);
        }

        return true;
    }

    @Override
    public boolean tryFollowOrUnfollowUser(User authenticatedUser, Long userId) {
        var user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return false;
        }

        var isTryingToFollowThemselves = Objects.equals(authenticatedUser, user);

        if (isTryingToFollowThemselves) {
            return false;
        }

        var follower = userRepository.findById(authenticatedUser.getId()).orElse(null);

        if (user.getFollowers().contains(follower)) {
            user.getFollowers().remove(follower);
        } else {
            user.getFollowers().add(follower);
        }

        userRepository.save(user);

        return true;
    }
}
