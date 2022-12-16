package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.TrackPostDto;
import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.enums.EAudioFileFormat;
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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;

@Service
@AllArgsConstructor
public class TrackServiceImpl implements TrackService {
    private final static int TEN_MEGABYTES = 10485760;
    private final List<String> allowedAudioFileTypes = newArrayList("audio/mpeg", "audio/wav");
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    @Override
    public boolean setUpdateTrackViewModel(User authenticatedUser, Long trackId, Model model, TrackPostDto trackPostDto) {
        var track = trackRepository.findById(trackId).orElse(null);

        if (track == null ||
                !Objects.equals(track.getAlbum().getUser().getId(), authenticatedUser.getId()) &&
                !authenticatedUser.isAdmin()) {
            return false;
        }

        var artists = userRepository.findAllByArtistNameIsNotNull();

        model.addAttribute("album", track.getAlbum());
        model.addAttribute("artists", artists);
        model.addAttribute("trackId", track.getId());

        if (trackPostDto == null) {
            var trackDto = new TrackPostDto();
            trackDto.setTrackName(track.getName());
            var usersIds = track.getUsers().stream().map(User::getId).toList();
            trackDto.setUsersIds(usersIds);
            model.addAttribute("track", trackDto);
        }

        return true;
    }

    @Override
    public Album tryUpdateTrack(User authenticatedUser,
                                TrackPostDto trackPostDto,
                                Long trackId,
                                BindingResult bindingResult,
                                Model model) throws UnsupportedAudioFileException, IOException {
        var isSuccessful = setUpdateTrackViewModel(authenticatedUser, trackId, model, trackPostDto);

        var track = trackRepository.findById(trackId).orElse(null);

        if (track == null || !isSuccessful || bindingResult.hasErrors()) {
            return null;
        }

        var file = trackPostDto.getAudioSource();
        var contentType = file.getContentType();
        var size = file.getSize();
        var isAudioSourcePresent = size > 0;

        if (isAudioSourcePresent && (!allowedAudioFileTypes.contains(contentType) || size > TEN_MEGABYTES)) {
            bindingResult.rejectValue("audioSource", "error.track",
                    "Please make sure the image is either jpeg or png, and the size is no bigger than 10MB.");
            return null;
        }

        if (isAudioSourcePresent) {
            var filename = fileService.save(trackPostDto.getAudioSource());

            var fileResource = fileService.load(filename);
            var audioFileFormat = Objects.equals(StringUtils.getFilenameExtension(filename), "wav") ?
                    EAudioFileFormat.WAV : EAudioFileFormat.MP3;
            var durationSeconds = getAudioDuration(fileResource, audioFileFormat);

            track.setAudioSourceUrl(filename);
            track.setDurationSeconds(durationSeconds != null ? durationSeconds.intValue() : 0);
        }

        var artists = new ArrayList<User>();
        trackPostDto.getUsersIds().forEach(userId -> {
            userRepository.findById(userId).ifPresent(artists::add);
        });

        track.setUsers(artists);
        track.setName(trackPostDto.getTrackName());

        trackRepository.save(track);

        return track.getAlbum();
    }

    @Override
    public Long tryDeleteTrack(User authenticatedUser, Long trackId) {
        var track = trackRepository.findById(trackId).orElse(null);

        if (track == null) {
            return null;
        }

        var album = track.getAlbum();
        var albumOwner = album.getUser();

        if (!Objects.equals(authenticatedUser.getId(), albumOwner.getId()) && !authenticatedUser.isAdmin()) {
            return null;
        }

        trackRepository.delete(track);
        return album.getId();
    }

    @Override
    public Float getAudioDuration(Resource fileResource, EAudioFileFormat audioFileFormat)
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
