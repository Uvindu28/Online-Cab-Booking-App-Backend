package com.system.megacitycab.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface PasswordService {
    ResponseEntity<?> sendPasswordResetOtp(String email);
    ResponseEntity<?> resetPassword(String email, String otp, String newPassword);
}
