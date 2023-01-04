package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.ui.Model;

public interface SearchService {
    void setSearchModel(User authenticatedUser, Model model, String keyword);
}
