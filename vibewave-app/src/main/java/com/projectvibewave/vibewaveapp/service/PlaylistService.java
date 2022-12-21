package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.AlbumPostDto;
import com.projectvibewave.vibewaveapp.dto.PlaylistPostDto;
import com.projectvibewave.vibewaveapp.entity.Playlist;
import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static com.google.common.collect.Lists.newArrayList;

public interface PlaylistService {
    boolean setPlaylistFormViewModel(Model model, User authenticatedUser, Long playlistId);

    Playlist tryCreatePlaylist(User authenticatedUser,
                               PlaylistPostDto playlistPostDto,
                               BindingResult bindingResult,
                               Model model);

    boolean setPlaylistViewModel(User authenticatedUser, Model model, Long playlistId);

    boolean tryDeletePlaylist(User authenticatedUser, Long playlistId);

    boolean tryEditPlaylist(User authenticatedUser, PlaylistPostDto playlistPostDto, BindingResult bindingResult, Model model);
}
