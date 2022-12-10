package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.TrackPostDto;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.repository.TrackRepository;
import com.projectvibewave.vibewaveapp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Objects;

@Service
@AllArgsConstructor
public class TrackServiceImpl implements TrackService {
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;

    @Override
    public boolean setUpdateTrackViewModel(User authenticatedUser, Long trackId, Model model) {
        var track = trackRepository.findById(trackId).orElse(null);

        if (authenticatedUser == null ||
                track == null ||
                !Objects.equals(track.getAlbum().getUser().getId(), authenticatedUser.getId()) &&
                        !authenticatedUser.isAdmin()) {
            return false;
        }

        var artists = userRepository.findAllByArtistNameIsNotNull();

        model.addAttribute("album", track.getAlbum());
        model.addAttribute("artists", artists);
        var trackDto = new TrackPostDto();
        trackDto.setTrackName(track.getName());
        var usersIds = track.getUsers().stream().map(User::getId).toList();
        trackDto.setUsersIds(usersIds);
        model.addAttribute("track", trackDto);
        model.addAttribute("trackId", track.getId());

        return true;
    }

    @Override
    public String tryUpdateTrack(Long trackId, Model model) {
        return "";
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
}
