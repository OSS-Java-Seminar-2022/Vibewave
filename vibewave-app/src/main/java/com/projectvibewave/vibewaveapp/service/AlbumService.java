package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.AlbumPostDto;
import com.projectvibewave.vibewaveapp.dto.TrackPostDto;
import com.projectvibewave.vibewaveapp.entity.Album;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

public interface AlbumService {
    void setAlbumPostPageModel(Model model);

    Album tryAddAlbum(AlbumPostDto albumPostDto, BindingResult bindingResult, Model model);

    boolean setAlbumByIdViewModel(Model model, Long albumId);

    boolean setAlbumAddTrackViewModel(Long albumId, Model model);

    boolean tryAddTrackToAlbum(Long albumId, TrackPostDto trackPostDto, BindingResult bindingResult, Model model);
}
