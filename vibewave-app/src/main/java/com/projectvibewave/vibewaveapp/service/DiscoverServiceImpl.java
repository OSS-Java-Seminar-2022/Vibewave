package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.repository.AlbumRepository;
import com.projectvibewave.vibewaveapp.repository.PlaylistRepository;
import com.projectvibewave.vibewaveapp.repository.TrackRepository;
import com.projectvibewave.vibewaveapp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DiscoverServiceImpl implements DiscoverService {
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

    @Override
    public void setFreshContentModel(Model model) {
        var freshAlbums = albumRepository.findWhereArtistIsVerifiedOrderByPublishDateDesc();

        model.addAttribute("albums", freshAlbums.stream().limit(10).toList());
    }

    @Override
    public void setHotContentModel(Model model) {
        var hotAlbums = albumRepository.findWhereArtistIsVerifiedOrderByTotalPlaysDesc();

        model.addAttribute("albums", hotAlbums.stream().limit(10).toList());
    }

    @Override
    public void setFollowedArtistContentModel(Long userId, Model model) {
        var freshAlbumsByFollowedArtists =
                albumRepository.findWhereArtistIsVerifiedAndUserIsFollowerOrderByPublishDateDesc(userId);

        var freshAlbumsByFollowedArtistsLimited = freshAlbumsByFollowedArtists.stream().limit(10).toList();

        Map<String, List<Album>> freshAlbumsByFollowedArtistsGrouped =
                freshAlbumsByFollowedArtistsLimited.stream().collect(Collectors.groupingBy(album ->
                        album.getUser().getArtistName() != null ?
                                album.getUser().getArtistName() :
                                album.getUser().getUsername()));

        model.addAttribute("artistAlbums", freshAlbumsByFollowedArtistsGrouped);
    }
}
