package com.system.megacitycab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    private String bookingId;

    private String customerId;

    private String carId;

    private String driverId;

    private String pickupLocation;

    private String destination;

    private LocalDateTime bookingDate;

    private LocalDateTime pickupDate;

    private double totalAmount;

    private double tax;

    private boolean completed = false;

    private boolean driverRequired = false;

    private BookingStatus status = BookingStatus.PENDING;

    private String cancellationReason;

    private LocalDateTime cancellationTime;

    private boolean refundIssued = false;

    private double refundAmount;

    public boolean canBeCancelled(){
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }
}
