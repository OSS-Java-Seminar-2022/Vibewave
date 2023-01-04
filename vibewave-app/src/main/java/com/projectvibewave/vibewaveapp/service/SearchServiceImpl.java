package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.repository.AlbumRepository;
import com.projectvibewave.vibewaveapp.repository.PlaylistRepository;
import com.projectvibewave.vibewaveapp.repository.TrackRepository;
import com.projectvibewave.vibewaveapp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Objects;

@Service
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    @Override
    public void setSearchModel(User authenticatedUser, Model model, String keyword) {
        if (keyword.length() < 3) {
            model.addAttribute("users", new ArrayList<>());
            model.addAttribute("albums", new ArrayList<>());
            model.addAttribute("playlists", new ArrayList<>());
            model.addAttribute("tracks", new ArrayList<>());
            return;
        }

        var users =
                userRepository.findAllByArtistNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(keyword, keyword);
        var albums = albumRepository.findAllByNameContainingIgnoreCase(keyword);
        var playlists = playlistRepository.findAllByNameContainingIgnoreCase(keyword);
        var tracks = trackRepository.findAllByNameContainingIgnoreCase(keyword);

        if (authenticatedUser == null || !authenticatedUser.isAdmin()) {
            users = users.stream().filter(
                    user -> !user.getPrivate() ||
                            (authenticatedUser != null &&
                                    Objects.equals(user.getId(), authenticatedUser.getId()))).toList();
            playlists = playlists.stream().filter(
                    playlist -> !playlist.isPrivate() ||
                            (authenticatedUser != null &&
                                    Objects.equals(playlist.getUser().getId(), authenticatedUser.getId()))).toList();
        }

        model.addAttribute("users", users);
        model.addAttribute("albums", albums);
        model.addAttribute("playlists", playlists);
        model.addAttribute("tracks", tracks);
    }
}
