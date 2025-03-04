package com.system.megacitycab.Admin.serviceImpl;

import java.util.List;

import com.system.megacitycab.Admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.system.megacitycab.Admin.model.Admin;
import com.system.megacitycab.Admin.repository.AdminRepository;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    public Admin getAdminById(String adminId) {
        return adminRepository.findById(adminId).orElse(null);
    }

    @Override
    public Admin createAdmin(Admin admin) {
        String encodedPassword = passwordEncoder.encode(admin.getPassword());
        admin.setPassword(encodedPassword);
        return adminRepository.save(admin);
    }
    
}
