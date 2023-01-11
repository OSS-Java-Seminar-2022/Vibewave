package com.projectvibewave.vibewaveapp.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(indexes = {
        @Index(name = "roleNameIndex", columnList = "role_name", unique = true),
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", nullable = false)
    private Long id;
    @Column(name = "role_name", nullable = false, unique = true)
    @Size(min = 1, max = 255)
    private String name;
    @ManyToMany(mappedBy = "roles")
    @ToString.Exclude
    private Set<User> users;

    public Role(String name) {
        this.name = name;
    }
}
