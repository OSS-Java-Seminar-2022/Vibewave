package com.projectvibewave.vibewaveapp;

import com.google.common.collect.Sets;
import com.projectvibewave.vibewaveapp.entity.Role;
import com.projectvibewave.vibewaveapp.entity.User;
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
    private RoleService roleService;

    @Resource
    private UserService userService;

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

        roleService.saveAll(allRoles);

        var basicRole = roleService.findByName("ROLE_BASIC");
        var premiumRole = roleService.findByName("ROLE_PREMIUM");
        var adminRole = roleService.findByName("ROLE_ADMIN");

        if (basicRole.isEmpty() || premiumRole.isEmpty() || adminRole.isEmpty()) {
            throw new RuntimeException("Roles were not inserted properly at startup.");
        }

        var basicUser = User.builder()
                .username("basic")
                .email("basic@user.com")
                .password(passwordEncoder.encode("basic"))
                .isEnabled(true)
                .roles(Sets.newHashSet(basicRole.get()))
                .build();

        var adminUser = User.builder()
                .username("admin")
                .email("admin@user.com")
                .password(passwordEncoder.encode("admin"))
                .isEnabled(true)
                .roles(Sets.newHashSet(basicRole.get(), premiumRole.get(), adminRole.get()))
                .build();

        var users = List.of(adminUser, basicUser);
        userService.saveAll(users);

        logger.info("database populated!");
    }
}
