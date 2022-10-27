package com.projectvibewave.vibewaveapp.security;

public enum UserPermission {
    TRACK_READ("track:read"),
    TRACK_WRITE("track:write"),
    ALBUM_READ("album:read"),
    ALBUM_WRITE("album:write"),
    PLAYLIST_READ("playlist:read"),
    PLAYLIST_WRITE("playlist:write"),
    STAFF_SELECTED_PLAYLIST_READ("staff_selected_playlist:read"),
    STAFF_SELECTED_PLAYLIST_WRITE("staff_selected_playlist:write"),
    FOLLOW_READ("follow:read"),
    FOLLOW_WRITE("follow:write"),
    USER_READ("user:read"),
    USER_WRITE("user:write"),
    ALBUM_FORMATS_READ("album_formats:read"),
    ALBUM_FORMATS_WRITE("album_formats:write");

    private final String permission;

    UserPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

}
