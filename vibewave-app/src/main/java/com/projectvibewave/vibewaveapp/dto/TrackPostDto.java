package com.projectvibewave.vibewaveapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackPostDto {
    @NotNull
    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^([A-z0-9]+\\s)*[A-z0-9]+$", message = "Only letters and numbers are allowed. " +
            "Make sure you only insert one space between words.")
    private String trackName;
    @NotNull
    private MultipartFile audioSource;
    private List<Long> usersIds = new ArrayList<>();
}
