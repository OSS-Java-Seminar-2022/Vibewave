package com.projectvibewave.vibewaveapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpDto {
    @NotNull
    @Size(min = 3, max = 255)
    @Pattern(regexp = "[A-z0-9_]+", message = "Only letters, numbers and _ are allowed")
    private String username;
    @Email
    @NotNull
    @Size(min = 3, max = 255)
    private String email;
    @NotNull
    @Size(min = 6, max = 255)
    private String password;
    @NotNull
    @Size(min = 6, max = 255)
    private String repeatedPassword;
}
