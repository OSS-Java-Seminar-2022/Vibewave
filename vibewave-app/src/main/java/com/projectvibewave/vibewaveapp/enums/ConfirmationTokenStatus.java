package com.projectvibewave.vibewaveapp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConfirmationTokenStatus {
    NOT_FOUND("Token invalid"),
    ALREADY_CONFIRMED("E-Mail has already been confirmed"),
    SUCCESSFULLY_CONFIRMED("E-Mail has been successfully confirmed"),
    EXPIRED("Token has expired");

    private final String message;
}
