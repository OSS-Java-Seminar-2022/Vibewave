package com.projectvibewave.vibewaveapp.controller;

import com.projectvibewave.vibewaveapp.dto.AdminEditUserDto;
import com.projectvibewave.vibewaveapp.dto.AlbumPostDto;
import com.projectvibewave.vibewaveapp.dto.StaffSelectionsDto;
import com.projectvibewave.vibewaveapp.service.AdminService;
import com.projectvibewave.vibewaveapp.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Controller
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin")
public class AdminController {
    private final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;

    @GetMapping
    public String getPanelView() {
        logger.info("Accessed Admin Panel Home Page");

        return "admin/panel";
    }

    @GetMapping("/users")
    public String getUsersView(Model model, @RequestParam(name = "page", required = true, defaultValue = "1") Long pageNumber) {
        logger.info("Accessed Admin Panel - Users Page");

        adminService.setUsersModel(model, pageNumber);

        return "admin/users";
    }

    @GetMapping("/user/{userId}/edit")
    @PreAuthorize("#userId != authentication.principal.id")
    public String getEditUserView(Model model, @PathVariable @NotNull Long userId) {
        logger.info("Accessed Admin Panel - User Edit Page");

        var isSuccessful = adminService.setUserEditModel(model, userId);

        if (!isSuccessful) {
            return "redirect:/admin";
        }

        return "admin/user-edit";
    }

    @PostMapping("/user/{userId}/edit")
    @PreAuthorize("#userId != authentication.principal.id")
    public String tryEditUser(@PathVariable @NotNull Long userId,
                              @Valid @ModelAttribute("user") AdminEditUserDto adminEditUserDto,
                              BindingResult bindingResult,
                              Model model) {
        logger.info("trying to edit user - admin panel");

        var isSuccessful = adminService.tryEditUser(userId, adminEditUserDto, bindingResult, model);

        if (!isSuccessful) {
            return "admin/user-edit";
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/user/{userId}/delete")
    public String deleteUser(@PathVariable @NotNull Long userId) {
        logger.info("trying to delete user - admin panel");

        var isSuccessful = adminService.tryDeleteUser(userId);

        return "redirect:/admin/users" + (isSuccessful ? "?deleted" : "");
    }

    @GetMapping("/staff-selections")
    public String getStaffSelectionsView(Model model) {
        logger.info("Accessed Admin Panel - Staff Selections Page");

        adminService.setStaffSelectionsModel(model);

        return "admin/staff-selections";
    }

    @PostMapping("/staff-selections")
    public String updateStaffSelections(@ModelAttribute("staffSelections") StaffSelectionsDto staffSelectionsDto) {
        logger.info("updating staff selections - admin panel");

        adminService.updateStaffSelections(staffSelectionsDto);

        return "redirect:/admin/staff-selections";
    }

    @GetMapping("/verification-requests")
    public String getVerificationRequestsView(Model model) {
        logger.info("Accessed Admin Panel - Verification Requests Page");

        adminService.setVerificationRequestsModel(model);

        return "admin/verification-requests";
    }

    @PostMapping("/verification-requests/{reqId}/approve")
    public String approveVerificationRequest(@PathVariable @NotNull Long reqId) {
        logger.info("try approve verification request - admin panel");

        adminService.tryApproveVerificationRequest(reqId);

        return "redirect:/admin/verification-requests";
    }

    @PostMapping("/verification-requests/{reqId}/reject")
    public String rejectVerificationRequest(@PathVariable @NotNull Long reqId) {
        logger.info("try reject verification request - admin panel");

        adminService.tryRejectVerificationRequest(reqId);

        return "redirect:/admin/verification-requests";
    }
}
