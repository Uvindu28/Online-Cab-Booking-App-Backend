package com.system.megacitycab.service;

import com.system.megacitycab.dto.BookingRequest;
import com.system.megacitycab.dto.CancellationRequest;
import com.system.megacitycab.model.Booking;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookingService {
    List<Booking> getAllBookings();
    Booking getBookingById(String bookingId);
    Booking createBooking(BookingRequest request);
    Booking cancelBooking(String customerId, CancellationRequest request);
    List<Booking> getCustomerBookings(String customerId);
    Booking getBookingDetails(String customerId, String bookingId);
}
