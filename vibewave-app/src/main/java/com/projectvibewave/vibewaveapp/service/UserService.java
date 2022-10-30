package com.projectvibewave.vibewaveapp.service;

import com.google.common.collect.Sets;
import com.projectvibewave.vibewaveapp.dto.UserSignUpDto;
import com.projectvibewave.vibewaveapp.entity.Role;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.repository.RoleRepository;
import com.projectvibewave.vibewaveapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.HashSet;

@Service
public class UserService implements UserDetailsService {
    private final static String ROLE_PREFIX = "ROLE_";
    private final static String DEFAULT_ROLE_NAME = "BASIC";
    public final static String USER_NOT_FOUND_MSG = "User %s not found";
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, username))
        );
    }

    public boolean trySignUp(UserSignUpDto userDto, BindingResult bindingResult) {
        if (!userDto.getPassword().equals(userDto.getRepeatedPassword())) {
            bindingResult.rejectValue("repeatedPassword", "error.user", "Passwords do not match");
        }

        var usernameExists = userRepository.existsByUsername(userDto.getUsername());
        if (usernameExists) {
            bindingResult.rejectValue("username", "error.user", "Username already exists");
        }

        var emailExists = userRepository.existsByEmail(userDto.getEmail());
        if (emailExists) {
            bindingResult.rejectValue("email", "error.user", "E-Mail already exists");
        }

        if (bindingResult.hasErrors()) {
            return false;
        }

        var defaultRole = roleRepository.findByName(ROLE_PREFIX + DEFAULT_ROLE_NAME).orElseThrow(() ->
                new RuntimeException("Default role was not found in database")
        );

        var user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .email(userDto.getEmail())
                .roles(Sets.newHashSet(defaultRole))
                .build();

        userRepository.save(user);

        return true;
    }
}
