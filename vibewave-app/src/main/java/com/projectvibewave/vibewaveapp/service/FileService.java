package com.projectvibewave.vibewaveapp.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

public interface FileService {
    void init();
    String save(MultipartFile file);
    void save(ByteArrayOutputStream file, String filename);
    Resource load(String filename);
    void deleteAll();
}
