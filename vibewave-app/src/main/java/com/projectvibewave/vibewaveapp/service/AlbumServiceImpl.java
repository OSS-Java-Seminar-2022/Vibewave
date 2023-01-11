package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.AddTrackToPlaylistDto;
import com.projectvibewave.vibewaveapp.dto.AlbumPostDto;
import com.projectvibewave.vibewaveapp.dto.TrackPostDto;
import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.Track;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.enums.EAudioFileFormat;
import com.projectvibewave.vibewaveapp.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

import javax.sound.sampled.*;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
public class AlbumServiceImpl implements AlbumService {
    private final static int BASIC_ROLE_ALBUM_LIMIT = 1;
    private final AlbumRepository albumRepository;
    private final AlbumFormatRepository albumFormatRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;
    private final FileService fileService;
    private final TrackService trackService;

    @Override
    public boolean setAlbumFormViewModel(Model model, User authenticatedUser, Long albumId) {
        var hasExceededAlbumUploadLimit = false;

        var albumDto = new AlbumPostDto();
        Album album = null;
        if (albumId != null) {
            album = albumRepository.findById(albumId).orElse(null);

            if (authenticatedUser == null ||
                    album == null ||
                    !Objects.equals(album.getUser().getId(), authenticatedUser.getId()) &&
                    !authenticatedUser.isAdmin()) {
                return false;
            }

            albumDto.setAlbumId(albumId);
            albumDto.setAlbumFormatId(album.getAlbumFormat().getId());
            albumDto.setAlbumName(album.getName());
            albumDto.setPublishDate(album.getPublishDate());
            model.addAttribute("isEdit", true);
        } else {
            model.addAttribute("isEdit", false);

            if (!authenticatedUser.isPremium() && !authenticatedUser.isAdmin()) {
                var albumsByUser = albumRepository.findAllByUser(authenticatedUser);
                if (albumsByUser.size() >= BASIC_ROLE_ALBUM_LIMIT) {
                    hasExceededAlbumUploadLimit = true;
                }
            }
        }

        var albumFormats = albumFormatRepository.findAll();
        model.addAttribute("albumFormats", albumFormats);
        if (!model.containsAttribute("album")) {
            model.addAttribute("album", albumDto);
        }

        model.addAttribute("hasExceededAlbumUploadLimit", hasExceededAlbumUploadLimit);
        return true;
    }

    @Override
    public Album tryAddAlbum(User authenticatedUser,
                             AlbumPostDto albumPostDto,
                             BindingResult bindingResult,
                             Model model) {
        setAlbumFormViewModel(model, authenticatedUser, null);

        if (!authenticatedUser.isPremium() && !authenticatedUser.isAdmin()) {
            var albumsByUser = albumRepository.findAllByUser(authenticatedUser);
            if (albumsByUser.size() > BASIC_ROLE_ALBUM_LIMIT) {
                return null;
            }
        }

        if (bindingResult.hasErrors()) {
            return null;
        }

        if (albumPostDto.getPublishDate().isAfter(
                LocalDate.now().plusMonths(ALBUM_UPLOAD_MAX_MONTHS_BEFORE_RELEASE))) {
            bindingResult.rejectValue("publishDate", "error.album",
                    "Publish date can't be more than " + ALBUM_UPLOAD_MAX_MONTHS_BEFORE_RELEASE + " months from now.");
            return null;
        }

        var file = albumPostDto.getCoverPhoto();
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

        var albumFormat = albumFormatRepository.findById(albumPostDto.getAlbumFormatId()).orElse(null);

        var newAlbum = Album.builder()
                .name(albumPostDto.getAlbumName())
                .publishDate(albumPostDto.getPublishDate())
                .albumFormat(albumFormat)
                .coverPhotoUrl(filename)
                .user(authenticatedUser)
                .build();

        return albumRepository.save(newAlbum);
    }

    @Override
    public boolean tryEditAlbum(User authenticatedUser,
                                AlbumPostDto albumPostDto,
                                BindingResult bindingResult,
                                Model model) {
        var isSuccessful = setAlbumFormViewModel(model, authenticatedUser, albumPostDto.getAlbumId());

        if (!isSuccessful || bindingResult.hasErrors()) {
            return false;
        }

        var file = albumPostDto.getCoverPhoto();
        var contentType = file.getContentType();
        var size = file.getSize();
        var isCoverPhotoPresent = size > 0;

        if (isCoverPhotoPresent && (!FileService.ALLOWED_IMAGE_FILE_TYPES.contains(contentType) || size > FileService.TEN_MEGABYTES)) {
            bindingResult.rejectValue("coverPhoto", "error.album",
                    "Please make sure the image is either jpeg or png, and the size is no bigger than 10MB.");
            return false;
        }

        var albumFormat = albumFormatRepository.findById(albumPostDto.getAlbumFormatId()).orElse(null);
        var album = albumRepository.findById(albumPostDto.getAlbumId()).orElse(null);

        if (album == null) {
            return false;
        }

        album.setAlbumFormat(albumFormat);
        album.setName(albumPostDto.getAlbumName());
        album.setPublishDate(albumPostDto.getPublishDate());
        album.setPublishDate(albumPostDto.getPublishDate());
        if (isCoverPhotoPresent) {
            var filename = fileService.save(file);
            album.setCoverPhotoUrl(filename);
        }

        albumRepository.save(album);

        return true;
    }

    @Override
    public boolean setAlbumByIdViewModel(Model model, Long albumId, User authenticatedUser) {
        var album = albumRepository.findById(albumId).orElse(null);

        if (album == null) {
            return false;
        }

        model.addAttribute("album", album);
        model.addAttribute("addTrackToPlaylist", new AddTrackToPlaylistDto());
        if (authenticatedUser != null) {
            var playlists = playlistRepository.findAllByUser(authenticatedUser);
            model.addAttribute("playlists", playlists);
        }

        return true;
    }

    @Override
    public boolean setAlbumAddTrackViewModel(User authenticatedUser, Long albumId, Model model) {
        var album = albumRepository.findById(albumId).orElse(null);
        var artists = userRepository.findAllMutuallyFollowedUsers(authenticatedUser.getId());

        if (album == null ||
                !Objects.equals(album.getUser().getId(), authenticatedUser.getId()) && !authenticatedUser.isAdmin()) {
            return false;
        }

        model.addAttribute("album", album);
        model.addAttribute("artists", artists);
        if (!model.containsAttribute("track")) {
            model.addAttribute("track", new TrackPostDto());
        }
        model.addAttribute("trackId", null);

        return true;
    }

    @Override
    public boolean tryAddTrackToAlbum(User authenticatedUser,
                                      Long albumId,
                                      TrackPostDto trackPostDto,
                                      BindingResult bindingResult,
                                      Model model)
            throws UnsupportedAudioFileException, IOException {
        var album = albumRepository.findById(albumId).orElse(null);

        var isSuccessful = setAlbumAddTrackViewModel(authenticatedUser, albumId, model);
        if (!isSuccessful || bindingResult.hasErrors()) {
            return false;
        }

        var file = trackPostDto.getAudioSource();
        var contentType = file.getContentType();
        var size = file.getSize();

        if (!FileService.ALLOWED_AUDIO_FILE_TYPES.contains(contentType) || size > FileService.TEN_MEGABYTES) {
            bindingResult.rejectValue("audioSource", "error.track",
                    "Please make sure the audio is either mp3 or wav, and the size is no bigger than 10MB.");
            return false;
        }

        var mutuallyFollowedUsers = userRepository.findAllMutuallyFollowedUsersAsSet(authenticatedUser.getId());
        var involvedArtists = new HashSet<User>();
        involvedArtists.add(authenticatedUser);

        trackPostDto.getUsersIds().forEach(userId -> {
            var involvedArtist = userRepository.findById(userId).orElse(null);
            if (involvedArtist != null && mutuallyFollowedUsers.contains(involvedArtist)) {
                involvedArtists.add(involvedArtist);
            }
        });

        var filename = fileService.save(trackPostDto.getAudioSource());

        var fileResource = fileService.load(filename);
        var audioFileFormat = Objects.equals(StringUtils.getFilenameExtension(filename), "wav") ?
                EAudioFileFormat.WAV : EAudioFileFormat.MP3;
        var durationSeconds = trackService.getAudioDuration(fileResource, audioFileFormat);

        var newTrack = Track.builder()
                .album(album)
                .name(trackPostDto.getTrackName())
                .audioSourceUrl(filename)
                .durationSeconds(durationSeconds != null ? durationSeconds.intValue() : 0)
                .users(involvedArtists)
                .build();

        trackRepository.save(newTrack);

        return true;
    }

    @Override
    @Transactional
    public boolean tryDeleteAlbum(User authenticatedUser, Long albumId) {
        var album = albumRepository.findById(albumId).orElse(null);

        if (album == null) {
            return false;
        }

        var albumOwner = album.getUser();
        var authenticatedUserIsOwner = Objects.equals(authenticatedUser.getId(), albumOwner.getId());

        if (!authenticatedUserIsOwner && !authenticatedUser.isAdmin()) {
            return false;
        }

        albumRepository.delete(album);

        return true;
    }
}
