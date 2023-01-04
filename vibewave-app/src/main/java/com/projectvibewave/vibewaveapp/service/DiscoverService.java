package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.entity.User;
import org.springframework.ui.Model;

public interface DiscoverService {
    void setSearchModel(User authenticatedUser, Model model, String keyword);

    void setFreshContentModel(Model model);

    void setHotContentModel(Model model);

    void setFollowedArtistContentModel(Long userId, Model model);
}
