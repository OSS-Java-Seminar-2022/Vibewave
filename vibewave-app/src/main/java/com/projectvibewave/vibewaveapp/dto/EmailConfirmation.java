package com.projectvibewave.vibewaveapp.dto;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfirmation {
    @Email
    @NotNull
    @Size(min = 3, max = 255)
    private String email;
}
