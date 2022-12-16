package com.projectvibewave.vibewaveapp.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

@Service
public class GoogleDriveServiceImpl implements GoogleDriveService {

    @Override
    public ByteArrayOutputStream downloadFile(String realFileId) throws IOException {
        // Load pre-authorized user credentials from the environment.
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(Arrays.asList(DriveScopes.DRIVE_READONLY));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
                credentials);

        // Build a new authorized API client service.
        Drive service = new Drive.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("Drive samples")
                .build();

        try {
            OutputStream outputStream = new ByteArrayOutputStream();

            service.files().get(realFileId)
                    .executeMediaAndDownloadTo(outputStream);

            return (ByteArrayOutputStream) outputStream;
        } catch (GoogleJsonResponseException e) {
            throw new RuntimeException("Unable to move file: " + e.getDetails());
        }
    }
}
