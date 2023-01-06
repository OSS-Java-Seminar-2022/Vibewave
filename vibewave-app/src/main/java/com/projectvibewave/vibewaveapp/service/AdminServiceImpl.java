package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.AdminEditUserDto;
import com.projectvibewave.vibewaveapp.dto.StaffSelectionsDto;
import com.projectvibewave.vibewaveapp.entity.Album;
import com.projectvibewave.vibewaveapp.entity.StaffSelection;
import com.projectvibewave.vibewaveapp.entity.VerificationRequest;
import com.projectvibewave.vibewaveapp.enums.VerificationRequestStatus;
import com.projectvibewave.vibewaveapp.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final static String VERIFICATION_REQUEST_UPDATE_SUBJECT = "VibeWave - Verification Request Update";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PlaylistRepository playlistRepository;
    private final AlbumRepository albumRepository;
    private final StaffSelectionRepository staffSelectionRepository;
    private final TrackRepository trackRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final EmailService emailService;

    @Override
    public void setUsersModel(Model model, Long pageNumber) {
        var users = userRepository.findAll(
                PageRequest.of(pageNumber.intValue() - 1, PAGE_SIZE, Sort.by("createdAt").descending()));
        model.addAttribute("users", users);
    }

    @Override
    public boolean setUserEditModel(Model model, Long userId) {
        var user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return false;
        }

        var userDto = new AdminEditUserDto();
        userDto.setEmail(user.getEmail());
        userDto.setUsername(user.getUsername());
        userDto.setArtistName(user.getArtistName());
        userDto.setPrivate(user.getPrivate());
        userDto.setEnabled(user.isEnabled());
        userDto.setPremium(user.isPremium());
        userDto.setVerified(user.isPremium());
        userDto.setAdmin(user.isAdmin());

        model.addAttribute("user", userDto);
        model.addAttribute("userId", userId);

        return true;
    }

    @Override
    public boolean tryEditUser(Long userId, AdminEditUserDto adminEditUserDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return false;
        }

        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        var emailExists = userRepository.existsByEmail(adminEditUserDto.getEmail());
        var isEmailChanged = !Objects.equals(adminEditUserDto.getEmail(), user.getEmail());
        if (emailExists && isEmailChanged) {
            bindingResult.rejectValue("email", "error.user", "E-Mail is already being used.");
            return false;
        }

        var usernameExists = userRepository.existsByUsername(adminEditUserDto.getUsername());
        var isUsernameChanged = !Objects.equals(adminEditUserDto.getUsername(), user.getUsername());
        if (usernameExists && isUsernameChanged) {
            bindingResult.rejectValue("username", "error.user", "Username is already being used.");
            return false;
        }

        var artistNameExists = userRepository.existsByArtistName(adminEditUserDto.getArtistName());
        var isArtistNameChanged = !Objects.equals(adminEditUserDto.getArtistName(), user.getArtistName());
        if (artistNameExists && isArtistNameChanged) {
            bindingResult.rejectValue("artistName", "error.user", "Artist name is already being used.");
            return false;
        }

        user.setEmail(adminEditUserDto.getEmail());
        user.setUsername(adminEditUserDto.getUsername());
        user.setArtistName(adminEditUserDto.getArtistName());
        user.setPrivate(adminEditUserDto.isPrivate());
        user.setEnabled(adminEditUserDto.isEnabled());
        user.setVerified(adminEditUserDto.isVerified());

        if (adminEditUserDto.isAdmin() != user.isAdmin()) {
            var adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
            if (adminRole == null) {
                return false;
            }
            if (adminEditUserDto.isAdmin()) {
                user.addRole(adminRole);
            } else {
                user.removeRole(adminRole);
            }
        }

        if (adminEditUserDto.isPremium() != user.isPremium()) {
            var premiumRole = roleRepository.findByName("ROLE_PREMIUM").orElse(null);
            if (premiumRole == null) {
                return false;
            }
            if (adminEditUserDto.isPremium()) {
                user.addRole(premiumRole);
            } else {
                user.removeRole(premiumRole);
            }
        }

        userRepository.save(user);

        return true;
    }

    @Override
    public void setStaffSelectionsModel(Model model) {
        var playlists = playlistRepository.findAllByIsPrivateFalse();
        var staffSelections = staffSelectionRepository.findAll();

        var staffSelectionsDto = new StaffSelectionsDto();
        staffSelectionsDto.setPlaylistsIds(staffSelections.stream().map(ss -> ss.getSelectedPlaylist().getId()).toList());

        model.addAttribute("playlists", playlists);
        model.addAttribute("staffSelections", staffSelectionsDto);
    }

    @Override
    public void updateStaffSelections(StaffSelectionsDto staffSelectionsDto) {
        var staffSelections = staffSelectionRepository.findAll();
        staffSelections.forEach(ss -> {
            if (!staffSelectionsDto.getPlaylistsIds().contains(ss.getId())) {
                staffSelectionRepository.delete(ss);
            }
        });

        staffSelectionsDto.getPlaylistsIds().forEach(playlistId -> {
            var playlist = playlistRepository.findById(playlistId).orElse(null);
            if (playlist != null) {
                var staffSelection = staffSelectionRepository.findBySelectedPlaylist(playlist).orElse(null);
                if (staffSelection == null) {
                    var newStaffSelection = StaffSelection.builder().selectedPlaylist(playlist).build();
                    staffSelectionRepository.save(newStaffSelection);
                }
            }
        });
    }

    @Override
    public boolean tryDeleteUser(Long userId) {
        var user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return false;
        }

        var albumsByUser = albumRepository.findAllByUser(user);
        albumRepository.deleteAllInBatch(albumsByUser);
        var playlistsByUser = playlistRepository.findAllByUser(user);
        playlistRepository.deleteAllInBatch(playlistsByUser);
        var tracksByUser = trackRepository.findAllByUser(user.getId());
        tracksByUser.forEach(track -> {
            track.getUsers().remove(user);
        });
        var verificationRequests = verificationRequestRepository.findAllByUser(user);
        verificationRequestRepository.deleteAllInBatch(verificationRequests);
        var staffSelectionsByUser = staffSelectionRepository.findAllBySelectedPlaylist_User(user);
        staffSelectionRepository.deleteAllInBatch(staffSelectionsByUser);
        userRepository.delete(user);
        return true;
    }

    @Override
    public void setVerificationRequestsModel(Model model) {
        var verificationRequests = verificationRequestRepository.findAll();
        Map<String, List<VerificationRequest>> verificationRequestsGrouped =
                verificationRequests.stream().collect(Collectors.groupingBy(vr -> vr.getStatus().name()));

        if (!verificationRequestsGrouped.containsKey(VerificationRequestStatus.PENDING.name())) {
            verificationRequestsGrouped.put(VerificationRequestStatus.PENDING.name(), new ArrayList<>());
        }

        if (!verificationRequestsGrouped.containsKey(VerificationRequestStatus.APPROVED.name())) {
            verificationRequestsGrouped.put(VerificationRequestStatus.APPROVED.name(), new ArrayList<>());
        }

        if (!verificationRequestsGrouped.containsKey(VerificationRequestStatus.REJECTED.name())) {
            verificationRequestsGrouped.put(VerificationRequestStatus.REJECTED.name(), new ArrayList<>());
        }

        model.addAttribute("verificationRequests", verificationRequestsGrouped);
    }

    @Override
    public boolean tryApproveVerificationRequest(Long reqId) {
        var verificationRequest = verificationRequestRepository.findById(reqId).orElse(null);
        if (verificationRequest == null || verificationRequest.getStatus() != VerificationRequestStatus.PENDING) {
            return false;
        }

        verificationRequest.setDateResolved(LocalDateTime.now());
        verificationRequest.setStatus(VerificationRequestStatus.APPROVED);

        var user = verificationRequest.getUser();
        user.setVerified(true);

        verificationRequestRepository.save(verificationRequest);
        userRepository.save(user);

        var templateModel = new HashMap<String, Object>();
        templateModel.put("username", user.getUsername());
        templateModel.put("status", "APPROVED");
        emailService.sendHtml(
                user.getEmail(), VERIFICATION_REQUEST_UPDATE_SUBJECT, templateModel,
                EmailService.VERIFICATION_REQUEST_UPDATE_TEMPLATE);
        return true;
    }

    @Override
    public boolean tryRejectVerificationRequest(Long reqId) {
        var verificationRequest = verificationRequestRepository.findById(reqId).orElse(null);
        if (verificationRequest == null || verificationRequest.getStatus() != VerificationRequestStatus.PENDING) {
            return false;
        }

        verificationRequest.setDateResolved(LocalDateTime.now());
        verificationRequest.setStatus(VerificationRequestStatus.REJECTED);

        verificationRequestRepository.save(verificationRequest);

        var user = verificationRequest.getUser();
        var templateModel = new HashMap<String, Object>();
        templateModel.put("username", user.getUsername());
        templateModel.put("status", "REJECTED");
        emailService.sendHtml(
                user.getEmail(), VERIFICATION_REQUEST_UPDATE_SUBJECT, templateModel,
                EmailService.VERIFICATION_REQUEST_UPDATE_TEMPLATE);
        return true;
    }
}
