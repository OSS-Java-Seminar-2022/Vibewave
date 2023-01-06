package com.projectvibewave.vibewaveapp.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_selection_id", nullable = false)
    private Long id;
    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "selected_playlist_id", referencedColumnName = "playlist_id", nullable = false)
    private Playlist selectedPlaylist;
}
