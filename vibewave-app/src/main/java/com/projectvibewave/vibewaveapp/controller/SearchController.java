package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.entity.User;
import com.projectvibewave.vibewaveapp.service.SearchService;
import com.projectvibewave.vibewaveapp.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Controller
@AllArgsConstructor
@RequestMapping("/search")
public class SearchController {
    private final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final SearchService searchService;

    @GetMapping
    public String getSearchResults(Authentication authentication, Model model, @RequestParam @NotNull @NotBlank String keyword) {
        var authenticatedUser = authentication != null ? (User)authentication.getPrincipal() : null;

        searchService.setSearchModel(authenticatedUser, model, keyword);

        return "search/results";
    }

}
