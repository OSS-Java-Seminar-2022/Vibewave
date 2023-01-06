package com.projectvibewave.vibewaveapp.entity;

import com.projectvibewave.vibewaveapp.service.AlbumService;
import lombok.*;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id", nullable = false)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name="album_format_id")
    private AlbumFormat albumFormat;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name="user_id")
    private User user;
    private LocalDate publishDate;
    @Builder.Default
    private LocalDateTime uploadDate = LocalDateTime.now();
    private String coverPhotoUrl;
    @ToString.Exclude
    @OneToMany(mappedBy = "album")
    private List<Track> tracks;

    public boolean isReleased() {
        return !getPublishDate().isAfter(LocalDate.now());
    }
}
