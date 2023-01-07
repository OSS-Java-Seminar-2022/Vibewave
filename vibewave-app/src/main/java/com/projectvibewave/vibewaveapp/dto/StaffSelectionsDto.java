package com.projectvibewave.vibewaveapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffSelectionsDto {
    private List<Long> playlistsIds = new ArrayList<>();
}
