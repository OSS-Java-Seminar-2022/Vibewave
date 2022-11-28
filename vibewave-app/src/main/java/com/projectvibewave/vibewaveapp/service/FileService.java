package com.projectvibewave.vibewaveapp.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileService {
    void init();
    String save(MultipartFile file);
    Resource load(String filename);
    void deleteAll();
}
