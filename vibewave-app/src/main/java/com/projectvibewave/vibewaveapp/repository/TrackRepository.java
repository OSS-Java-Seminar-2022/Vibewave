package com.projectvibewave.vibewaveapp.repository;

import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.Track;
import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    @Query("select distinct t from Track t join t.users u where u.id = ?1")
    List<Track> findAllByUser(Long userId);
    Optional<Track> findTrackByAudioSourceUrl(String url);
    void deleteAllByAlbum(Album album);
    List<Track> findAllByNameContainingIgnoreCase(String name);
}
