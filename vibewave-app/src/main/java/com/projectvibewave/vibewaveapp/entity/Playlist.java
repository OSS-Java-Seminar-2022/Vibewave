package com.projectvibewave.vibewaveapp.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_id", nullable = false)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    private boolean isPrivate = false;
    private String coverPhotoUrl;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @ToString.Exclude
    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "playlists_tracks",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "track_id")
    )
    private Set<Track> tracks;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name="user_id")
    private User user;
}
