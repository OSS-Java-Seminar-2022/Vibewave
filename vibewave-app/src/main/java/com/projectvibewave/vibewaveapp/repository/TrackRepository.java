package com.projectvibewave.vibewaveapp.repository;

import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {
    Optional<Track> findTrackByAudioSourceUrl(String url);
    void deleteAllByAlbum(Album album);
}
