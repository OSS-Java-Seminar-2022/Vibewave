package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.PlaylistPostDto;
import com.projectvibewave.vibewaveapp.entity.Playlist;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.repository.PlaylistRepository;
import com.projectvibewave.vibewaveapp.repository.TrackRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.transaction.Transactional;
import java.util.Objects;


@Service
@AllArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;
    private final FileService fileService;

    @Override
    public boolean setPlaylistFormViewModel(Model model, User authenticatedUser, Long playlistId) {
        var playlistPostDto = new PlaylistPostDto();

        if (playlistId != null) {
            var playlistToUpdate = playlistRepository.findById(playlistId).orElse(null);
            if (playlistToUpdate == null) {
                return false;
            }

            var authenticatedUserIsOwner = Objects.equals(authenticatedUser.getId(), playlistToUpdate.getUser().getId());

            if (!authenticatedUserIsOwner && !authenticatedUser.isAdmin()) {
                return false;
            }

            playlistPostDto.setPlaylistName(playlistToUpdate.getName());
            playlistPostDto.setPrivate(playlistToUpdate.isPrivate());
            playlistPostDto.setPlaylistId(playlistToUpdate.getId());
        }

        if (!model.containsAttribute("playlist")) {
            model.addAttribute("playlist", playlistPostDto);
        }

        return true;
    }

    @Override
    public Playlist tryCreatePlaylist(User authenticatedUser,
                                      PlaylistPostDto playlistPostDto,
                                      BindingResult bindingResult,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            return null;
        }

        var file = playlistPostDto.getCoverPhoto();
        var contentType = file.getContentType();
        var size = file.getSize();
        var isCoverPhotoPresent = size > 0;

        if (isCoverPhotoPresent && (!FileService.ALLOWED_IMAGE_FILE_TYPES.contains(contentType) || size > FileService.TEN_MEGABYTES)) {
            bindingResult.rejectValue("coverPhoto", "error.album",
                    "Please make sure the image is either jpeg or png, and the size is no bigger than 10MB.");
            return null;
        }

        String filename = null;
        if (isCoverPhotoPresent) {
            filename = fileService.save(file);
        }

        var newPlaylist = Playlist.builder()
                .name(playlistPostDto.getPlaylistName())
                .coverPhotoUrl(filename)
                .isPrivate(playlistPostDto.isPrivate())
                .user(authenticatedUser)
                .build();

        return playlistRepository.save(newPlaylist);
    }

    @Override
    public boolean setPlaylistViewModel(User authenticatedUser, Model model, Long playlistId) {
        var playlist = playlistRepository.findById(playlistId).orElse(null);

        if (playlist == null) {
            return false;
        }

        var authenticatedUserHasReadAccess = authenticatedUser != null &&
                (Objects.equals(authenticatedUser.getId(), playlist.getUser().getId()) ||
                        authenticatedUser.isAdmin());

        if (playlist.isPrivate() && !authenticatedUserHasReadAccess) {
            return false;
        }

        model.addAttribute("playlist", playlist);

        return true;
    }

    @Override
    @Transactional
    public boolean tryDeletePlaylist(User authenticatedUser, Long playlistId) {
        var playlist = playlistRepository.findById(playlistId).orElse(null);

        if (playlist == null) {
            return false;
        }

        var playlistOwner = playlist.getUser();

        var authenticatedUserIsOwner = Objects.equals(authenticatedUser.getId(), playlistOwner.getId());

        if (!authenticatedUserIsOwner && !authenticatedUser.isAdmin()) {
            return false;
        }

        playlistRepository.delete(playlist);

        return true;
    }

    @Override
    public boolean tryEditPlaylist(User authenticatedUser,
                                   PlaylistPostDto playlistPostDto,
                                   BindingResult bindingResult,
                                   Model model) {
        var isSuccessful = setPlaylistFormViewModel(model, authenticatedUser, playlistPostDto.getPlaylistId());

        if (!isSuccessful || bindingResult.hasErrors()) {
            return false;
        }

        var file = playlistPostDto.getCoverPhoto();
        var contentType = file.getContentType();
        var size = file.getSize();
        var isCoverPhotoPresent = size > 0;

        if (isCoverPhotoPresent && (!FileService.ALLOWED_IMAGE_FILE_TYPES.contains(contentType) || size > FileService.TEN_MEGABYTES)) {
            bindingResult.rejectValue("coverPhoto", "error.album",
                    "Please make sure the image is either jpeg or png, and the size is no bigger than 10MB.");
            return false;
        }

        var playlist = playlistRepository.findById(playlistPostDto.getPlaylistId()).orElse(null);

        if (playlist == null) {
            return false;
        }

        playlist.setName(playlistPostDto.getPlaylistName());
        playlist.setPrivate(playlistPostDto.isPrivate());
        if (isCoverPhotoPresent) {
            var filename = fileService.save(file);
            playlist.setCoverPhotoUrl(filename);
        }

        playlistRepository.save(playlist);

        return true;
    }

    @Override
    public boolean tryRemoveTrackFromPlaylist(User authenticatedUser, Long playlistId, Long trackId) {
        var playlist = playlistRepository.findById(playlistId).orElse(null);
        var trackToRemove = trackRepository.findById(trackId).orElse(null);

        if (playlist == null || trackToRemove == null) {
            return false;
        }

        var authenticatedUserIsOwner = Objects.equals(authenticatedUser.getId(), playlist.getUser().getId());

        if (!authenticatedUserIsOwner && !authenticatedUser.isAdmin()) {
            return false;
        }

        playlist.getTracks().remove(trackToRemove);
        playlistRepository.save(playlist);

        return true;
    }
}
