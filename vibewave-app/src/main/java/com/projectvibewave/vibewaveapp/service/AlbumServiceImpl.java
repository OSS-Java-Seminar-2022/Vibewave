package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.AlbumPostDto;
import com.projectvibewave.vibewaveapp.dto.TrackPostDto;
import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.Track;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.enums.EAudioFileFormat;
import com.projectvibewave.vibewaveapp.repository.AlbumFormatRepository;
import com.projectvibewave.vibewaveapp.repository.AlbumRepository;
import com.projectvibewave.vibewaveapp.repository.TrackRepository;
import com.projectvibewave.vibewaveapp.repository.UserRepository;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

import javax.sound.sampled.*;
import javax.transaction.Transactional;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;

@Service
@AllArgsConstructor
public class AlbumServiceImpl implements AlbumService {
    private final static int TEN_MEGABYTES = 10485760;
    private final List<String> allowedImageFileTypes = newArrayList("image/jpeg", "image/png");
    private final List<String> allowedAudioFileTypes = newArrayList("audio/mpeg", "audio/wav");
    private final AlbumRepository albumRepository;
    private final AlbumFormatRepository albumFormatRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final FileService fileService;

    @Override
    public boolean setAlbumFormViewModel(Model model, User authenticatedUser, Long albumId) {
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
        }

        var albumFormats = albumFormatRepository.findAll();
        model.addAttribute("albumFormats", albumFormats);
        if (!model.containsAttribute("album")) {
            model.addAttribute("album", albumDto);
        }

        return true;
    }

    @Override
    public Album tryAddAlbum(User authenticatedUser,
                             AlbumPostDto albumPostDto,
                             BindingResult bindingResult,
                             Model model) {
        setAlbumFormViewModel(model, null, null);

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

        if (isCoverPhotoPresent && (!allowedImageFileTypes.contains(contentType) || size > TEN_MEGABYTES)) {
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
        if (!model.containsAttribute("track")) {
            model.addAttribute("track", new TrackPostDto());
        }
        model.addAttribute("trackId", null);

        return true;
    }

    @Override
    public boolean tryAddTrackToAlbum(User authenticatedUser,
                                      Long albumId, TrackPostDto trackPostDto,
                                      BindingResult bindingResult,
                                      Model model)
            throws UnsupportedAudioFileException, IOException {
        var album = albumRepository.findById(albumId).orElse(null);

        var isSuccessful = setAlbumAddTrackViewModel(albumId, model);
        if (!isSuccessful) {
            throw new RuntimeException("Album not found");
        }

        if (album == null ||
                !Objects.equals(album.getUser().getId(), authenticatedUser.getId()) && !authenticatedUser.isAdmin() ||
                bindingResult.hasErrors()) {
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
            userRepository.findById(userId).ifPresent(artists::add);
        });

        var filename = fileService.save(trackPostDto.getAudioSource());

        var fileResource = fileService.load(filename);
        var audioFileFormat = Objects.equals(StringUtils.getFilenameExtension(filename), "wav") ?
                EAudioFileFormat.WAV : EAudioFileFormat.MP3;
        var durationSeconds = getAudioDuration(fileResource, audioFileFormat);

        var newTrack = Track.builder()
                .album(album)
                .name(trackPostDto.getTrackName())
                .audioSourceUrl(filename)
                .durationSeconds(durationSeconds != null ? durationSeconds.intValue() : 0)
                .users(artists)
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

        if (!Objects.equals(authenticatedUser.getId(), albumOwner.getId()) && !authenticatedUser.isAdmin()) {
            return false;
        }

        trackRepository.deleteAllByAlbum(album);
        albumRepository.delete(album);

        return true;
    }

    private Float getAudioDuration(Resource fileResource, EAudioFileFormat audioFileFormat)
            throws UnsupportedAudioFileException, IOException {
        switch(audioFileFormat) {
            case WAV -> {
                InputStream targetStream = new BufferedInputStream(fileResource.getInputStream());
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(targetStream);
                AudioFormat format = audioInputStream.getFormat();
                long frames = audioInputStream.getFrameLength();
                return frames / format.getFrameRate();
            }
            case MP3 -> {
                AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(fileResource.getFile());
                Map<String, Object> properties = baseFileFormat.properties();
                var duration = (Long) properties.get("duration");
                return duration.floatValue() / 1000000;
            }
            default -> {
                return null;
            }
        }
    }
}
