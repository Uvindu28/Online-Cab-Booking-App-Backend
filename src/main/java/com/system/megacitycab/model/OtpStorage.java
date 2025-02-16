package com.system.megacitycab.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "otp_storage")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpStorage {
    @Id
    private String email;

    private String otp;

    private Instant expiryTime;
 
}
