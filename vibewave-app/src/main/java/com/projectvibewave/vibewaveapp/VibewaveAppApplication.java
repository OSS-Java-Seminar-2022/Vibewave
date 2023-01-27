package com.projectvibewave.vibewaveapp;

import com.google.common.collect.Sets;
import com.projectvibewave.vibewaveapp.entity.*;
import com.projectvibewave.vibewaveapp.repository.*;
import com.projectvibewave.vibewaveapp.service.FileService;
import com.projectvibewave.vibewaveapp.service.GoogleDriveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;

@SpringBootApplication
public class VibewaveAppApplication implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(VibewaveAppApplication.class);

    @Resource
    private UserRepository userRepository;

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private AlbumFormatRepository albumFormatRepository;

    @Resource
    private AlbumRepository albumRepository;

    @Resource
    private TrackRepository trackRepository;

    @Resource
    private PlaylistRepository playlistRepository;

    @Resource
    private StaffSelectionRepository staffSelectionRepository;

    @Resource
    private VerificationRequestRepository verificationRequestRepository;

    @Resource
    private FileService fileService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private GoogleDriveService googleDriveService;

    public static void main(String[] args) {
        SpringApplication.run(VibewaveAppApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("populating database...");

        fileService.deleteAll();
        fileService.init();

        // download images and audio from Google Drive
        var defaultProfilePhoto =
                googleDriveService.downloadFile(GoogleDriveService.DEFAULT_PROFILE_PHOTO_FILE_ID);
        fileService.save(defaultProfilePhoto, "default-profile.png");

        var defaultAlbumPhoto =
                googleDriveService.downloadFile(GoogleDriveService.DEFAULT_ALBUM_COVER_FILE_ID);
        fileService.save(defaultAlbumPhoto, "default-album.png");

        var empowerTrack =
                googleDriveService.downloadFile(GoogleDriveService.EMPOWER_TRACK_REAL_ID);
        fileService.save(empowerTrack, "empower.mp3");

        var theMottoRemixTrack =
                googleDriveService.downloadFile(GoogleDriveService.THE_MOTTO_REMIX_TRACK_REAL_ID);
        fileService.save(theMottoRemixTrack, "the-motto-remix.mp3");

        var allRoles = List.of(
                Role.builder()
                        .name("ROLE_BASIC")
                        .build(),
                Role.builder()
                        .name("ROLE_PREMIUM")
                        .build(),
                Role.builder()
                        .name("ROLE_ADMIN")
                        .build()
        );

        roleRepository.saveAll(allRoles);

        var basicRole = roleRepository.findByName("ROLE_BASIC").orElse(null);
        var premiumRole = roleRepository.findByName("ROLE_PREMIUM").orElse(null);
        var adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);

        if (basicRole == null || premiumRole == null || adminRole == null) {
            throw new RuntimeException("Roles were not inserted properly at startup.");
        }

        var basicUser = User.builder()
                .username("basic")
                .email("basic@user.com")
                .password(passwordEncoder.encode("basic"))
                .isEnabled(true)
                .artistName("DJ Basic")
                .isVerified(true)
                .roles(Sets.newHashSet(basicRole))
                .build();

        var adminUser = User.builder()
                .username("admin")
                .email("admin@user.com")
                .password(passwordEncoder.encode("admin"))
                .isEnabled(true)
                .artistName("DJ Admin")
                .roles(Sets.newHashSet(basicRole, premiumRole, adminRole))
                .build();

        var premiumUser = User.builder()
                .username("premium")
                .email("premium@user.com")
                .password(passwordEncoder.encode("premium"))
                .isEnabled(true)
                .artistName("DJ Premium")
                .roles(Sets.newHashSet(basicRole, premiumRole))
                .followers(Set.of(basicUser))
                .build();

        basicUser.setFollowers(Set.of(premiumUser));

        var users = List.of(adminUser, premiumUser, basicUser);
        userRepository.saveAll(users);

        var albumFormats = newArrayList(
                AlbumFormat.builder()
                        .name("Single")
                        .build(),
                AlbumFormat.builder()
                        .name("EP")
                        .build(),
                AlbumFormat.builder()
                        .name("LP")
                        .build(),
                AlbumFormat.builder()
                        .name("Double LP")
                        .build(),
                AlbumFormat.builder()
                        .name("Mixtape")
                        .build()
        );

        albumFormatRepository.saveAll(albumFormats);

        var albums = newArrayList(
                Album.builder()
                        .name("Stories")
                        .publishDate(LocalDate.of(2015, 10, 2))
                        .albumFormat(albumFormats.get(1))
                        .coverPhotoUrl(null)
                        .user(basicUser)
                        .build()
        );

        var tracks = newArrayList(
                Track.builder()
                        .name("Empower")
                        .album(albums.get(0))
                        .audioSourceUrl("empower.mp3")
                        .durationSeconds(203)
                        .users(Set.copyOf(users))
                        .build(),
                Track.builder()
                        .name("The Motto Remix")
                        .album(albums.get(0))
                        .audioSourceUrl("the-motto-remix.mp3")
                        .durationSeconds(166)
                        .users(Set.of(basicUser))
                        .build()
        );

        albumRepository.saveAll(albums);
        trackRepository.saveAll(tracks);

        var playlists = newArrayList(
                Playlist.builder()
                        .name("Top 100 2022")
                        .coverPhotoUrl(null)
                        .isPrivate(false)
                        .user(basicUser)
                        .tracks(Sets.newHashSet(tracks))
                        .build()
        );

        playlistRepository.saveAll(playlists);

        var staffSelections = newArrayList(
                StaffSelection.builder()
                        .selectedPlaylist(playlists.get(0))
                        .build()
        );

        staffSelectionRepository.saveAll(staffSelections);

        var message = """
                Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the 
                industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and 
                scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap 
                into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the
                 release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing
                  software like Aldus PageMaker including versions of Lorem Ipsum.
                """;

        var verificationsRequests = newArrayList(
                VerificationRequest.builder()
                        .user(premiumUser)
                        .message(message)
                        .build()
        );

        verificationRequestRepository.saveAll(verificationsRequests);

        logger.info("database populated!");
    }
}
