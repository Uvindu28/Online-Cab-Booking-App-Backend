package com.system.megacitycab.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private String customerId;
    private String carId;
    private String pickupLocation;
    private String destination;
    private LocalDateTime pickupDate;
    private boolean driverRequired;

}
