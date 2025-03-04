package com.system.megacitycab.Booking.model;

import com.system.megacitycab.Booking.Enum.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


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

    private String bookingDate;

    private String pickupDate;

    private double totalAmount;

    private double tax;

    private boolean completed = false;

    private boolean driverRequired = false;

    private BookingStatus status = BookingStatus.PENDING;

    private String cancellationReason;

    private String cancellationTime;

    private boolean refundIssued = false;

    private double refundAmount;

    public boolean canBeCancelled(){
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

    public boolean canBeDeleted() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }
}
