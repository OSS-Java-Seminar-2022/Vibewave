package com.projectvibewave.vibewaveapp;

import com.projectvibewave.vibewaveapp.entity.Role;
import com.projectvibewave.vibewaveapp.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class VibewaveAppApplication implements CommandLineRunner {
	private final Logger logger = LoggerFactory.getLogger(VibewaveAppApplication.class);
	@Resource
	private RoleRepository roleRepository;

	public static void main(String[] args) {
		SpringApplication.run(VibewaveAppApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("populating database...");

		var roles = new HashSet<Role>();
		roles.add(new Role("ROLE_BASIC"));
		roles.add(new Role("ROLE_PREMIUM"));
		roles.add(new Role("ROLE_ADMIN"));

		roleRepository.saveAll(roles);
	}
}
