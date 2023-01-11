package com.projectvibewave.vibewaveapp.repository;

import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByArtistName(String artistName);
    @Query("select distinct flr from User u join u.following flg join u.followers flr where u.id = ?1")
    List<User> findAllMutuallyFollowedUsers(Long userId);
    @Query("select distinct flr from User u join u.following flg join u.followers flr where u.id = ?1")
    Set<User> findAllMutuallyFollowedUsersAsSet(Long userId);
    List<User> findAllByArtistNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(String artistName, String username);
    @Query("select sum(t.timesPlayed) from Track t join t.users u where u.id = ?1")
    Long getTotalPlaysByUser(Long userId);
}
