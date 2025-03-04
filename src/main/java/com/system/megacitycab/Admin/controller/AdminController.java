package com.system.megacitycab.Admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.system.megacitycab.Admin.model.Admin;
import com.system.megacitycab.Admin.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/auth/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("/getalladmins")
    public List<Admin> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    @GetMapping("/getadmin/{adminId}")
    public Admin getAdminById(@PathVariable String adminId) {
        return adminService.getAdminById(adminId);
    }

    @PostMapping("/createadmin")
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin admin) {
        Admin createAdmin = adminService.createAdmin(admin);
        return ResponseEntity.status(201).body(createAdmin);
    }
}
