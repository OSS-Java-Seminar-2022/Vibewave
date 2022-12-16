package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.repository.TrackRepository;
import com.projectvibewave.vibewaveapp.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Controller
@RequestMapping("/file")
@AllArgsConstructor
public class FileController {
    private final FileService fileService;
    private final TrackRepository trackRepository;

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> download(@PathVariable @NotNull @NotBlank String filename) throws IOException {
        var resource = fileService.load(filename);

        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        var track = trackRepository.findTrackByAudioSourceUrl(filename).orElse(null);
        if (track != null) {
            track.setTimesPlayed(track.getTimesPlayed() + 1);
            trackRepository.save(track);
        }

        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
