package com.projectvibewave.vibewaveapp.repository;

import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.Playlist;
import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    boolean existsAlbumById(Long albumId);
    List<Album> findAllByNameContainingIgnoreCase(String name);
    @Query("select a from Album a join a.user u where u.isVerified = true order by a.publishDate DESC")
    List<Album> findWhereArtistIsVerifiedOrderByPublishDateDesc();
    @Query("select t.album from Track t join t.album a where a.user.isVerified = true group by a.id order by sum(t.timesPlayed) DESC")
    List<Album> findWhereArtistIsVerifiedOrderByTotalPlaysDesc();

    @Query("select distinct a from Album a join a.user u join u.followers f where u.isVerified = true and f.id = ?1 order by a.publishDate DESC")
    List<Album> findWhereArtistIsVerifiedAndUserIsFollowerOrderByPublishDateDesc(Long userId);
}
