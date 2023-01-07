package com.projectvibewave.vibewaveapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminEditUserDto {
    @NotNull
    @Size(min = 3, max = 20)
    @Pattern(regexp = "[A-z0-9_]+", message = "Only letters, numbers and _ are allowed")
    private String username;
    @Email
    @NotNull
    @Size(min = 3, max = 255)
    private String email;
    @Size(min = 3, max = 40)
    @Pattern(regexp = "^([A-z0-9]+\\s)*[A-z0-9]+$", message = "Only letters and numbers are allowed. " +
            "Make sure you only insert one space between words.")
    private String artistName;
    private boolean isPrivate;
    private boolean isEnabled;
    private boolean isVerified;
    private boolean isPremium;
    private boolean isAdmin;
}
