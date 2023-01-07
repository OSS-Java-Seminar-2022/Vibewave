package com.projectvibewave.vibewaveapp.repository;

import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.entity.VerificationRequest;
import com.projectvibewave.vibewaveapp.enums.VerificationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    List<VerificationRequest> findAllByUserOrderByStatusAscDateAppliedDesc(User user);
    List<VerificationRequest> findAllByUser(User user);
    List<VerificationRequest> findAllByStatus(VerificationRequestStatus status);
}
