package com.system.megacitycab.Booking.model;

import com.system.megacitycab.Booking.Enum.BookingStatus;
import com.system.megacitycab.Driver.model.Driver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    private String bookingId;

    private String customerId;

    private String customerEmail;

    private String carId;

    private String driverId;

    private String pickupLocation;

    private String destination;

    private String bookingDate;

    private String pickupDate;

    private String pickupTime;

    private double totalAmount;

    @Transient
    private String passengerName; // Added for frontend compatibility (mapped from Customer)

    @Transient
    private String passengerImage; // Added for frontend compatibility (mapped from Customer)

    @Transient
    private Double passengerRating; // Added for frontend compatibility (mapped from Customer)

    private double tax;

    private boolean completed = false;

    private boolean driverRequired = false;

    private BookingStatus status = BookingStatus.PENDING;

    private String cancellationReason;

    private String cancellationTime;

    private boolean refundIssued = false;

    private double refundAmount;

    @Transient
    private Driver driverDetails;

    public boolean canBeCancelled() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    public boolean canBeDeleted() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }
}