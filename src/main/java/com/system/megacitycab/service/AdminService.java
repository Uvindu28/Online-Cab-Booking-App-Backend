package com.system.megacitycab.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.system.megacitycab.model.Admin;

@Service
public interface AdminService {
    List<Admin> getAllAdmins();
    Admin getAdminById(String adminId);
    Admin createAdmin(Admin admin);
}
