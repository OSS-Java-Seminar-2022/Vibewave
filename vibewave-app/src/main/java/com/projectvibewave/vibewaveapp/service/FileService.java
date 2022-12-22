package com.projectvibewave.vibewaveapp.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public interface FileService {
    int TEN_MEGABYTES = 10485760;
    List<String> ALLOWED_IMAGE_FILE_TYPES = newArrayList("image/jpeg", "image/png");
    List<String> ALLOWED_AUDIO_FILE_TYPES = newArrayList("audio/mpeg", "audio/wav");

    void init();
    String save(MultipartFile file);
    void save(ByteArrayOutputStream file, String filename);
    Resource load(String filename);
    void deleteAll();
}
