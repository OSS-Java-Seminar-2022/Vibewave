package com.projectvibewave.vibewaveapp.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(indexes = {
        @Index(name = "playlistNameIndex", columnList = "name"),
})
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
    @Column(nullable = false)
    private String name;
    private boolean isPrivate = false;
    private String coverPhotoUrl;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @ToString.Exclude
    @ManyToMany(mappedBy = "playlists", fetch = FetchType.EAGER)
    private Set<Track> tracks;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id")
    private User user;
    @OneToOne(mappedBy = "selectedPlaylist", cascade = CascadeType.ALL)
    private StaffSelection staffSelection;
}
