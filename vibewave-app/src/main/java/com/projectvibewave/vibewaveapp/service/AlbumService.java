package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.AlbumPostDto;
import com.projectvibewave.vibewaveapp.dto.TrackPostDto;
import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface AlbumService {
    int ALBUM_UPLOAD_MAX_MONTHS_BEFORE_RELEASE = 6;

    boolean setAlbumFormViewModel(Model model, User authenticatedUser, Long albumId);

    Album tryAddAlbum(User authenticatedUser, AlbumPostDto albumPostDto, BindingResult bindingResult, Model model) throws IOException, UnsupportedAudioFileException;

    boolean tryEditAlbum(User authenticatedUser,
                         AlbumPostDto albumPostDto,
                         BindingResult bindingResult,
                         Model model);

    boolean setAlbumByIdViewModel(Model model, Long albumId, User authenticatedUser);

    boolean setAlbumAddTrackViewModel(User authenticatedUser, Long albumId, Model model);

    boolean tryAddTrackToAlbum(User authenticatedUser, Long albumId, TrackPostDto trackPostDto, BindingResult bindingResult, Model model) throws UnsupportedAudioFileException, IOException;

    boolean tryDeleteAlbum(User authenticatedUser, Long albumId);
}
