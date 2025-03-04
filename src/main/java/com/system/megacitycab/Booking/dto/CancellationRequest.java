package com.system.megacitycab.Booking.dto;

import lombok.Data;

@Data
public class CancellationRequest {
    private String bookingId;
    private String reason;
}
