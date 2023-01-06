package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.service.DiscoverService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Controller
@AllArgsConstructor
public class DiscoverController {
    private final Logger logger = LoggerFactory.getLogger(DiscoverController.class);
    private final DiscoverService discoverService;

    @GetMapping("/search")
    public String getSearchResults(Authentication authentication, Model model, @RequestParam @NotNull @NotBlank String keyword) {
        logger.info("Accessed Search Results Page");

        var authenticatedUser = authentication != null ? (User)authentication.getPrincipal() : null;

        discoverService.setSearchModel(authenticatedUser, model, keyword);

        return "discover/search-results";
    }

    @GetMapping("/fresh")
    public String getFreshContent(Model model) {
        logger.info("Accessed Fresh Page");

        discoverService.setFreshContentModel(model);

        return "discover/fresh";
    }

    @GetMapping("/hot")
    public String getHotContent(Model model) {
        logger.info("Accessed Hot Page");

        discoverService.setHotContentModel(model);

        return "discover/hot";
    }

    @GetMapping("/staff-selections")
    public String getStaffSelectionsContent(Model model) {
        logger.info("Accessed Staff Selections Page");

        discoverService.setStaffSelectionsContentModel(model);

        return "discover/staff-selections";
    }

    @GetMapping("/followed-artists")
    @PreAuthorize("isAuthenticated()")
    public String getHotContent(Authentication authentication, Model model) {
        logger.info("Accessed Followed Artists Page");

        var authenticatedUser = (User)authentication.getPrincipal();
        discoverService.setFollowedArtistContentModel(authenticatedUser.getId(), model);

        return "discover/followed-artists";
    }
}
