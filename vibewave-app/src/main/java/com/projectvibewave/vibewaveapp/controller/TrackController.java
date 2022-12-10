package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.service.TrackService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@Controller
@AllArgsConstructor
@RequestMapping("/track")
public class TrackController {
    private final Logger logger = LoggerFactory.getLogger(TrackController.class);
    private final TrackService trackService;

    @PostMapping("/{trackId}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteTrackById(Authentication authentication, @PathVariable @NotNull Long trackId) {
        logger.info("Accessed Delete Track By Id");

        var albumIdOrNull = trackService.tryDeleteTrack((User)authentication.getPrincipal(), trackId);
        
        if (albumIdOrNull == null) {
            return "redirect:/";
        }

        return "redirect:/album/" + albumIdOrNull + "?deleted-track";
    }

    @GetMapping("/{trackId}/edit")
    @PreAuthorize("isAuthenticated()")
    public String updateTrackById(Authentication authentication,
                                  @PathVariable @NotNull Long trackId,
                                  Model model) {
        logger.info("Accessed Update Track View");

        var isSuccessful = trackService.setUpdateTrackViewModel(
                (User)authentication.getPrincipal(), trackId, model);

        if (!isSuccessful) {
            return "redirect:/";
        }

        return "/album/add-track";
    }

    /*@PutMapping("/{trackId}")
    @PreAuthorize("isAuthenticated()")
    public String updateTrackById(Authentication authentication, @PathVariable @NotNull Long trackId) {
        logger.info("Accessed Update Track By Id");

        var albumIdOrNull = trackService.tryUpdateTrack((User)authentication.getPrincipal(), trackId);

        if (albumIdOrNull == null) {
            return "redirect:/";
        }

        return "redirect:/album/" + albumIdOrNull + "?deleted-track";
    }*/
}
