package com.projectvibewave.vibewaveapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.Empty;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsDto {
    private MultipartFile profilePhoto;
    @Size(min = 3, max = 255)
    @Pattern(regexp = "^([A-z0-9]+\\s)*[A-z0-9]+$", message = "Only letters and numbers are allowed. " +
            "Make sure you only insert one space between words.")
    private String artistName;
    private boolean isPrivate;
}
