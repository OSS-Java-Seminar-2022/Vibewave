package com.projectvibewave.vibewaveapp.repository;

import com.projectvibewave.vibewaveapp.entity.Playlist;
import com.projectvibewave.vibewaveapp.entity.StaffSelection;
import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffSelectionRepository extends JpaRepository<StaffSelection, Long> {
    Optional<StaffSelection> findBySelectedPlaylist(Playlist selectedPlaylist);
    List<StaffSelection> findAllBySelectedPlaylist_User(User user);

}
