package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.dto.AlbumPostDto;
import com.projectvibewave.vibewaveapp.dto.PlaylistPostDto;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.service.PlaylistService;
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
@RequestMapping("/playlist")
public class PlaylistController {
    private final Logger logger = LoggerFactory.getLogger(PlaylistController.class);
    private final PlaylistService playlistService;

    @GetMapping("/{playlistId}")
    public String getPlaylistView(Authentication authentication, Model model, @PathVariable @NotNull Long playlistId) {
        logger.info("Accessed Playlist View Page");

        var exists = playlistService.setPlaylistViewModel(
                authentication != null ? (User)authentication.getPrincipal() : null, model, playlistId);

        if (!exists) {
            return "redirect:/";
        }

        return "playlist/view-playlist";
    }

    @GetMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public String createPlaylistView(Model model) {
        logger.info("Accessed Playlist Add Page");

        playlistService.setPlaylistFormViewModel(model, null, null);

        return "playlist/add-playlist";
    }

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public String createPlaylist(Authentication authentication,
                          @Valid @ModelAttribute("playlist") PlaylistPostDto playlistPostDto,
                          BindingResult bindingResult,
                          Model model) {
        logger.info("trying to add playlist");

        var playlistOrNull = playlistService.tryCreatePlaylist((User)authentication.getPrincipal(), playlistPostDto, bindingResult, model);

        if (playlistOrNull == null) {
            return "playlist/add-playlist";
        }

        return "redirect:/playlist/" + playlistOrNull.getId();
    }

    @GetMapping("/{playlistId}/edit")
    @PreAuthorize("isAuthenticated()")
    public String playlistEditView(Authentication authentication,
                                @PathVariable Long playlistId,
                                Model model) {
        logger.info("Accessed Playlist Edit Page");

        var isSuccessful = playlistService.setPlaylistFormViewModel(model, (User)authentication.getPrincipal(), playlistId);

        if (!isSuccessful) {
            return "redirect:/";
        }

        return "playlist/add-playlist";
    }

    @PostMapping("/{playlistId}/edit")
    @PreAuthorize("isAuthenticated()")
    public String albumEdit(Authentication authentication,
                            @PathVariable @NotNull Long playlistId,
                            @Valid @ModelAttribute("playlist") PlaylistPostDto playlistPostDto,
                            BindingResult bindingResult,
                            Model model)
            throws UnsupportedAudioFileException, IOException {
        logger.info("Trying to edit playlist...");

        var isSuccessful = playlistService.tryEditPlaylist(
                (User)authentication.getPrincipal(), playlistPostDto, bindingResult, model);

        if (!isSuccessful) {
            return "playlist/add-playlist";
        }

        return "redirect:/playlist/" + playlistId + "?edit";
    }

    @PostMapping("/{playlistId}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deletePlaylistById(Authentication authentication, @PathVariable @NotNull Long playlistId) {
        logger.info("trying to delete playlist...");

        var authenticatedUser = (User)authentication.getPrincipal();
        var isSuccessful = playlistService.tryDeletePlaylist(authenticatedUser, playlistId);

        if (!isSuccessful) {
            return "redirect:/";
        }

        return "redirect:/user/" + authenticatedUser.getId() + "?deleted-playlist";
    }

    @PostMapping("/{playlistId}/remove/{trackId}")
    @PreAuthorize("isAuthenticated()")
    public String removeTrackFromPlaylist(Authentication authentication,
                                          @PathVariable @NotNull Long playlistId,
                                          @PathVariable @NotNull Long trackId) {
        logger.info("trying to remove track from playlist...");

        var isSuccessful = playlistService.tryRemoveTrackFromPlaylist(
                (User)authentication.getPrincipal(), playlistId, trackId);

        if (!isSuccessful) {
            return "redirect:/";
        }

        return "redirect:/playlist/" + playlistId + "?removed-track";
    }
}
