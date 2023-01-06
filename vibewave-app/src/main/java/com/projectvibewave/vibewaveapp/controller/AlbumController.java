package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.dto.AlbumPostDto;
import com.projectvibewave.vibewaveapp.dto.TrackPostDto;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.service.AlbumService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Controller
@AllArgsConstructor
@RequestMapping("/album")
public class AlbumController {
    private final Logger logger = LoggerFactory.getLogger(AlbumController.class);
    private final AlbumService albumService;

    @GetMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public String albumAddView(Authentication authentication, Model model) {
        logger.info("Accessed Album Add Page");

        var authenticatedUser = (User)authentication.getPrincipal();
        albumService.setAlbumFormViewModel(model, authenticatedUser, null);

        return "album/upload-album";
    }

    @GetMapping("/{albumId}/edit")
    @PreAuthorize("isAuthenticated()")
    public String albumEditView(Authentication authentication,
                                @PathVariable Long albumId,
                                Model model) {
        logger.info("Accessed Album Edit Page");

        var isSuccessful = albumService.setAlbumFormViewModel(model, (User)authentication.getPrincipal(), albumId);

        if (!isSuccessful) {
            return "redirect:/";
        }

        return "album/upload-album";
    }

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public String albumAdd(Authentication authentication,
                           @Valid @ModelAttribute("album") AlbumPostDto albumPostDto,
                           BindingResult bindingResult,
                           Model model)
            throws UnsupportedAudioFileException, IOException {
        logger.info("Trying to add album...");

        var createdAlbumOrNull = albumService.tryAddAlbum(
                (User)authentication.getPrincipal(), albumPostDto, bindingResult, model);

        if (createdAlbumOrNull == null) {
            return "album/upload-album";
        }

        return "redirect:/album/" + createdAlbumOrNull.getId() + "?success";
    }

    @PostMapping("/{albumId}/edit")
    @PreAuthorize("isAuthenticated()")
    public String albumEdit(Authentication authentication,
                            @PathVariable @NotNull Long albumId,
                            @Valid @ModelAttribute("album") AlbumPostDto albumPostDto,
                            BindingResult bindingResult,
                            Model model)
            throws UnsupportedAudioFileException, IOException {
        logger.info("Trying to edit album...");

        var isSuccessful = albumService.tryEditAlbum(
                (User)authentication.getPrincipal(), albumPostDto, bindingResult, model);

        if (!isSuccessful) {
            return "album/upload-album";
        }

        return "redirect:/album/" + albumId + "?edit";
    }

    @GetMapping("/{albumId}")
    public String getAlbumByIdView(Authentication authentication, Model model, @PathVariable @NotNull Long albumId) {
        logger.info("Accessed Album View Page");

        var exists = albumService.setAlbumByIdViewModel(
                model, albumId, authentication != null ? (User)authentication.getPrincipal() : null);

        if (!exists) {
            return "redirect:/";
        }

        return "album/view-album";
    }

    @GetMapping("/{albumId}/add-track")
    @PreAuthorize("isAuthenticated()")
    public String addTrackToAlbum(Authentication authentication, Model model, @PathVariable @NotNull Long albumId) {
        logger.info("Accessed Add Track To Album Page");

        var exists = albumService.setAlbumAddTrackViewModel(
                (User)authentication.getPrincipal(), albumId, model);

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
            isSuccessful = albumService.tryAddTrackToAlbum(
                    (User)authentication.getPrincipal(), albumId, trackPostDto, bindingResult, model);
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (!isSuccessful) {
            return "album/add-track";
        }

        return "redirect:/album/" + albumId + "?added-track";
    }

    @PostMapping("{albumId}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteAlbumById(Authentication authentication, @PathVariable @NotNull Long albumId) {
        logger.info("trying to delete album...");

        var authenticatedUser = (User)authentication.getPrincipal();
        var isSuccessful = albumService.tryDeleteAlbum(authenticatedUser, albumId);

        if (!isSuccessful) {
            return "redirect:/";
        }

        return "redirect:/user/" + authenticatedUser.getId() + "?deleted-album";
    }
}
