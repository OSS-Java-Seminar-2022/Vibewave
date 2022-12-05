package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.dto.AlbumPostDto;
import com.projectvibewave.vibewaveapp.dto.TrackPostDto;
import com.projectvibewave.vibewaveapp.dto.UserSettingsDto;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.service.AlbumService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/album")
public class AlbumController {
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AlbumService albumService;

    @GetMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public String albumAddView(Model model, Principal principal) {
        logger.info("Accessed Album Post Page");

        var username = principal.getName();

        albumService.setAlbumPostPageModel(model);

        return "album/upload-album";
    }

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public String albumAdd(@Valid @ModelAttribute("album") AlbumPostDto albumPostDto, BindingResult bindingResult, Model model)
            throws UnsupportedAudioFileException, IOException {
        logger.info("Trying to add album...");

        var createdAlbumOrNull = albumService.tryAddAlbum(albumPostDto, bindingResult, model);

        if (createdAlbumOrNull == null) {
            return "album/upload-album";
        }

        return "redirect:/album/" + createdAlbumOrNull.getId() + "?success";
    }

    @GetMapping("/{albumId}")
    public String getAlbumByIdView(Model model, @PathVariable @NotNull Long albumId) {
        logger.info("Accessed Album View Page");

        var exists = albumService.setAlbumByIdViewModel(model, albumId);

        if (!exists) {
            return "redirect:/";
        }

        return "album/view-album";
    }

    @GetMapping("/{albumId}/add-track")
    @PreAuthorize("isAuthenticated()")
    public String addTrackToAlbum(Model model, @PathVariable @NotNull Long albumId) {
        logger.info("Accessed Add Track To Album Page");

        var exists = albumService.setAlbumAddTrackViewModel(albumId, model);

        if (!exists) {
            return "redirect:/";
        }

        return "album/add-track";
    }

    @PostMapping("/{albumId}/add-track")
    @PreAuthorize("isAuthenticated()")
    public String tryAddTrackToAlbum(Authentication authentication,
                                     @PathVariable @NotNull Long albumId,
                                     @Valid @ModelAttribute("track") TrackPostDto trackPostDto,
                                     BindingResult bindingResult,
                                     Model model) {
        logger.info("Trying to add track...");

        boolean isSuccessful = false;
        try {
            isSuccessful = albumService.tryAddTrackToAlbum((User)authentication.getPrincipal(), albumId, trackPostDto, bindingResult, model);
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (!isSuccessful) {
            return "album/add-track";
        }

        return "redirect:/album/" + albumId + "?added-track";
    }
}
