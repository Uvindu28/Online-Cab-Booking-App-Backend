package com.system.megacitycab.serviceImpl;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

import com.system.megacitycab.service.EmailService;
import com.system.megacitycab.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.system.megacitycab.model.Customer;
import com.system.megacitycab.model.Driver;
import com.system.megacitycab.model.OtpStorage;
import com.system.megacitycab.repository.CustomerRepository;
import com.system.megacitycab.repository.DriverRepository;
import com.system.megacitycab.repository.OtpStorageRepository;

@Service
public class PasswordServiceImpl implements PasswordService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private OtpStorageRepository otpStorageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final int OTP_EXPIRATION_MINUTES = 5;

    @Override
    public ResponseEntity<?> sendPasswordResetOtp(String email) {
        Optional<Customer> customerOtp = customerRepository.findByEmail(email);
        Optional<Driver> driverOpt = driverRepository.findByEmail(email);

        if(!customerOtp.isPresent() && !driverOpt.isPresent()){
            return ResponseEntity.badRequest().body("Email not found");
        }

        String otp = generateOTP();
        Instant expiryTime = Instant.now().plusSeconds(OTP_EXPIRATION_MINUTES * 60);

        OtpStorage otpStorage = new OtpStorage(email, otp, expiryTime);
        otpStorageRepository.save(otpStorage);

        String emailBody = "Your OTP for password rest is: " + otp + "\nThis OTP will expire in " + OTP_EXPIRATION_MINUTES + " minutes.";
        emailService.sendEmail(email, "Password Reset OTP", emailBody);

        return ResponseEntity.ok("OTP sent to your email");
    }

    @Override
    public ResponseEntity<?> resetPassword(String email, String otp, String newPassword) {
        Optional<OtpStorage> otpStorageOpt = otpStorageRepository.findByEmail(email);

        if(!otpStorageOpt.isPresent()){
            return ResponseEntity.badRequest().body("No OTP request found");
        }
    
        OtpStorage otpStorage = otpStorageOpt.get();

        if(Instant.now().isAfter(otpStorage.getExpiryTime())){
            otpStorageRepository.deleteByEmail(email);
            return ResponseEntity.badRequest().body("OTP has expired");
        }

        if(!otpStorage.getOtp().equals(otp)){
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        if(!isValidPassword(newPassword)){
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long, include a number, and an uppercase letter.");

        }

        customerRepository.findByEmail(email).ifPresent(customer -> {
            customer.setPassword(passwordEncoder.encode(newPassword));
            customerRepository.save(customer);
        });

        driverRepository.findByEmail(email).ifPresent(driver -> {
            driver.setPassword(passwordEncoder.encode(newPassword));
            driverRepository.save(driver);
        });

        otpStorageRepository.deleteByEmail(email);

        return ResponseEntity.ok("Password update successfully");
    }

    private String generateOTP(){
        SecureRandom random = new SecureRandom();
        int otp = 100_000 + random.nextInt(900_000);
        return String.valueOf(otp);
    }

    private boolean isValidPassword(String password){
        return password.length() >= 8 && password.matches(".*\\d.*") && password.matches(".*[A-Z].*");
    }

    
}
