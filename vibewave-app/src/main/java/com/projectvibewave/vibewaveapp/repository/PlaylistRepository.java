package com.projectvibewave.vibewaveapp.repository;

import com.projectvibewave.vibewaveapp.entity.Playlist;
import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findAllByUser(User user);
    List<Playlist> findAllByNameContainingIgnoreCase(String name);
}
