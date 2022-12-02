package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.AlbumPostDto;
import com.projectvibewave.vibewaveapp.dto.TrackPostDto;
import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.Track;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.repository.AlbumFormatRepository;
import com.projectvibewave.vibewaveapp.repository.AlbumRepository;
import com.projectvibewave.vibewaveapp.repository.TrackRepository;
import com.projectvibewave.vibewaveapp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;

@Service
@AllArgsConstructor
public class AlbumServiceImpl implements AlbumService {
    private final static int TEN_MEGABYTES = 10485760;
    private final List<String> allowedImageFileTypes = newArrayList("image/jpeg", "image/png");
    private final List<String> allowedAudioFileTypes = newArrayList("audio/mpeg", "audio/x-wav");

    private final AlbumRepository albumRepository;
    private final AlbumFormatRepository albumFormatRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final FileService fileService;

    @Override
    public void setAlbumPostPageModel(Model model) {
        var albumFormats = albumFormatRepository.findAll();
        model.addAttribute("albumFormats", albumFormats);
        model.addAttribute("album", new AlbumPostDto());
    }

    @Override
    public Album tryAddAlbum(AlbumPostDto albumPostDto, BindingResult bindingResult, Model model) {
        var albumFormats = albumFormatRepository.findAll();
        model.addAttribute("albumFormats", albumFormats);

        if (bindingResult.hasErrors()) {
            return null;
        }

        var file = albumPostDto.getCoverPhoto();
        var contentType = file.getContentType();
        var size = file.getSize();
        var isCoverPhotoPresent = size > 0;

        if (isCoverPhotoPresent && (!allowedImageFileTypes.contains(contentType) || size > TEN_MEGABYTES)) {
            bindingResult.rejectValue("coverPhoto", "error.album",
                    "Please make sure the image is either jpeg or png, and the size is no bigger than 10MB.");
            return null;
        }

        var authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String filename = null;
        if (isCoverPhotoPresent) {
            filename = fileService.save(file);
        }

        var albumFormat = albumFormatRepository.findById(albumPostDto.getAlbumFormatId()).orElse(null);

        var newAlbum = Album.builder()
                .name(albumPostDto.getAlbumName())
                .publishDate(albumPostDto.getPublishDate())
                .albumFormat(albumFormat)
                .coverPhotoUrl(filename)
                .user(authenticatedUser)
                .build();

        var createdAlbum = albumRepository.save(newAlbum);

        return createdAlbum;
    }

    @Override
    public boolean setAlbumByIdViewModel(Model model, Long albumId) {
        var album = albumRepository.findById(albumId).orElse(null);

        if (album == null) {
            return false;
        }

        model.addAttribute("album", album);
        return true;
    }

    @Override
    public boolean setAlbumAddTrackViewModel(Long albumId, Model model) {
        var album = albumRepository.findById(albumId).orElse(null);
        var artists = userRepository.findAllByArtistNameIsNotNull();

        if (album == null) {
            return false;
        }

        model.addAttribute("album", album);
        model.addAttribute("artists", artists);
        model.addAttribute("track", new TrackPostDto());
        return true;
    }

    @Override
    public boolean tryAddTrackToAlbum(Long albumId, TrackPostDto trackPostDto, BindingResult bindingResult, Model model) {
        var album = albumRepository.findById(albumId).orElse(null);
        var authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (album == null || !Objects.equals(album.getUser().getId(), authenticatedUser.getId()) || bindingResult.hasErrors()) {
            return false;
        }

        var file = trackPostDto.getAudioSource();
        var contentType = file.getContentType();
        var size = file.getSize();

        if (!allowedAudioFileTypes.contains(contentType) || size > TEN_MEGABYTES) {
            bindingResult.rejectValue("audioSource", "error.track",
                    "Please make sure the image is either mp3 or wav, and the size is no bigger than 10MB.");
            return false;
        }

        var artists = new ArrayList<User>();
        trackPostDto.getUsersIds().forEach(userId -> {
            var user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                artists.add(user);
            }
        });

        var filename = fileService.save(trackPostDto.getAudioSource());

        var newTrack = Track.builder()
                .album(album)
                .name(trackPostDto.getTrackName())
                .audioSourceUrl(filename)
                .users(artists)
                .build();

        trackRepository.save(newTrack);

        return true;
    }
}
