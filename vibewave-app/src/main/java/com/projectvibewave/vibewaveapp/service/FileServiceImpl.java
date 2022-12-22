package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.controller.AuthController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;

@Service
public class FileServiceImpl implements FileService{
    private final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    private final Path root = Paths.get("uploads");
    private final String dot = ".";

    @Override
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Override
    public String save(MultipartFile file) {
        var fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        var uuid = UUID.randomUUID().toString();
        var filename = uuid + dot + fileExtension;
        var fullPath = this.root.resolve(filename);
        try {
            Files.copy(file.getInputStream(), fullPath);
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file with that name already exists.");
            }

            throw new RuntimeException(e.getMessage());
        }

        return filename;
    }

    @Override
    public void save(ByteArrayOutputStream file, String filename) {
        var fullPath = this.root.resolve(filename);
        try (OutputStream outputStream = new FileOutputStream(fullPath.toString())) {
            file.writeTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }
}
