package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.TrackPostDto;
import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.enums.EAudioFileFormat;
import org.springframework.core.io.Resource;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface TrackService {
    boolean setUpdateTrackViewModel(User authenticatedUser, Long trackId, Model model, TrackPostDto trackPostDto);

    Album tryUpdateTrack(User authenticatedUser,
                         TrackPostDto trackPostDto,
                         Long trackId,
                         BindingResult bindingResult,
                         Model model) throws UnsupportedAudioFileException, IOException;

    Long tryDeleteTrack(User authenticatedUser, Long trackId);

    Float getAudioDuration(Resource fileResource, EAudioFileFormat audioFileFormat)
            throws UnsupportedAudioFileException, IOException;
}
