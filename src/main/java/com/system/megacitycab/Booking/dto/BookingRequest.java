package com.system.megacitycab.Booking.dto;

import lombok.Data;

@Data
public class BookingRequest {
    private String customerId;
    private String carId;
    private String pickupLocation;
    private String destination;
    private String pickupDate;
    private boolean driverRequired;

}
