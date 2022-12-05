package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.AlbumPostDto;
import com.projectvibewave.vibewaveapp.dto.TrackPostDto;
import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface AlbumService {
    void setAlbumPostPageModel(Model model);

    Album tryAddAlbum(AlbumPostDto albumPostDto, BindingResult bindingResult, Model model) throws IOException, UnsupportedAudioFileException;

    boolean setAlbumByIdViewModel(Model model, Long albumId);

    boolean setAlbumAddTrackViewModel(Long albumId, Model model);

    boolean tryAddTrackToAlbum(User authenticatedUser, Long albumId, TrackPostDto trackPostDto, BindingResult bindingResult, Model model) throws UnsupportedAudioFileException, IOException;
}
