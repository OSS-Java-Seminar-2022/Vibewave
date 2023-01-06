package com.projectvibewave.vibewaveapp.service;

import com.projectvibewave.vibewaveapp.dto.AdminEditUserDto;
import com.projectvibewave.vibewaveapp.dto.StaffSelectionsDto;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

public interface AdminService {
    int PAGE_SIZE = 15;
    void setUsersModel(Model model, Long pageNumber);

    boolean setUserEditModel(Model model, Long userId);

    boolean tryEditUser(Long userId, AdminEditUserDto adminEditUserDto, BindingResult bindingResult, Model model);

    void setStaffSelectionsModel(Model model);

    void updateStaffSelections(StaffSelectionsDto staffSelectionsDto);

    boolean tryDeleteUser(Long userId);

    void setVerificationRequestsModel(Model model);

    boolean tryApproveVerificationRequest(Long reqId);

    boolean tryRejectVerificationRequest(Long reqId);
}
