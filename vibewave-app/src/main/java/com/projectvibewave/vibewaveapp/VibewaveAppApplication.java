package com.projectvibewave.vibewaveapp;

import com.google.common.collect.Sets;
import com.projectvibewave.vibewaveapp.entity.Role;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.repository.RoleRepository;
import com.projectvibewave.vibewaveapp.repository.UserRepository;
import com.projectvibewave.vibewaveapp.service.RoleService;
import com.projectvibewave.vibewaveapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import java.util.*;

@SpringBootApplication
public class VibewaveAppApplication implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(VibewaveAppApplication.class);

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(VibewaveAppApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("populating database...");

        var allRoles = List.of(
                Role.builder()
                        .name("ROLE_BASIC")
                        .build(),
                Role.builder()
                        .name("ROLE_PREMIUM")
                        .build(),
                Role.builder()
                        .name("ROLE_ADMIN")
                        .build()
        );

        roleRepository.saveAll(allRoles);

        var basicRole = roleRepository.findByName("ROLE_BASIC").orElse(null);
        var premiumRole = roleRepository.findByName("ROLE_PREMIUM").orElse(null);
        var adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);

        if (basicRole == null || premiumRole == null || adminRole == null) {
            throw new RuntimeException("Roles were not inserted properly at startup.");
        }

        var basicUser = User.builder()
                .username("basic")
                .email("basic@user.com")
                .password(passwordEncoder.encode("basic"))
                .isEnabled(true)
                .roles(Sets.newHashSet(basicRole))
                .build();

        var adminUser = User.builder()
                .username("admin")
                .email("admin@user.com")
                .password(passwordEncoder.encode("admin"))
                .isEnabled(true)
                .roles(Sets.newHashSet(basicRole, premiumRole, adminRole))
                .build();

        var users = List.of(adminUser, basicUser);
        userRepository.saveAll(users);

        logger.info("database populated!");
    }
}
