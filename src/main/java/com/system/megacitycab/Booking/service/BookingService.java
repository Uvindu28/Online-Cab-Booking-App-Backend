package com.system.megacitycab.Booking.service;

import com.system.megacitycab.Booking.dto.BookingRequest;
import com.system.megacitycab.Booking.dto.CancellationRequest;
import com.system.megacitycab.Booking.model.Booking;
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
    void deleteBooking(String customerId, String bookingId);
}
