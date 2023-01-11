package com.projectvibewave.vibewaveapp.entity;

import com.projectvibewave.vibewaveapp.enums.VerificationRequestStatus;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_request_id", nullable = false)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    @Builder.Default
    @Column(nullable = false)
    private VerificationRequestStatus status = VerificationRequestStatus.PENDING;
    @Builder.Default
    public LocalDateTime dateApplied = LocalDateTime.now();
    @Builder.Default
    public LocalDateTime dateResolved = null;
}
