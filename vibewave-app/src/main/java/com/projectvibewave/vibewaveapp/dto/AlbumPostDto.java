package com.projectvibewave.vibewaveapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlbumPostDto {
    private Long albumId;
    @NotBlank
    @NotNull
    @Size(max = 255)
    @Pattern(regexp = "^([A-z0-9]+\\s)*[A-z0-9]+$", message = "Only letters and numbers are allowed. " +
            "Make sure you only insert one space between words.")
    private String albumName;
    @NotNull
    @PastOrPresent
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;
    @NotNull
    private Long albumFormatId;
    private MultipartFile coverPhoto;
}
