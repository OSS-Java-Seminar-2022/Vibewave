package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.ui.Model;

public interface TrackService {
    boolean setUpdateTrackViewModel(User authenticatedUser, Long trackId, Model model);

    String tryUpdateTrack(Long trackId, Model model);

    Long tryDeleteTrack(User authenticatedUser, Long trackId);
}
