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
public class AlbumFormat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_format_id", nullable = false)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
}
