package com.projectvibewave.vibewaveapp.repository;

import com.projectvibewave.vibewaveapp.entity.AlbumFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumFormatRepository extends JpaRepository<AlbumFormat, Long> {
    Optional<AlbumFormat> getAlbumFormatByName(String name);
}
