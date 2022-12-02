package com.projectvibewave.vibewaveapp.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface GoogleDriveService {
    String DEFAULT_PROFILE_PHOTO_FILE_ID = "1MTf4yViZqdouzc85qChgDP8D9O0XO7aF";
    String DEFAULT_ALBUM_COVER_FILE_ID = "1IRyEGzwLC23CTaGCN2Kf1TRIQclhGZ6-";

    ByteArrayOutputStream downloadFile(String realFileId) throws IOException;
}
