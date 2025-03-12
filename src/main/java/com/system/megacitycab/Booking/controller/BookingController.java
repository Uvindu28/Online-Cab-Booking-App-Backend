package com.system.megacitycab.Booking.controller;

import com.system.megacitycab.Booking.model.Booking;
import com.system.megacitycab.Booking.dto.BookingRequest;
import com.system.megacitycab.Booking.dto.CancellationRequest;
import com.system.megacitycab.exception.ResourceNotFoundException;
import com.system.megacitycab.Customer.model.Customer;
import com.system.megacitycab.Customer.repository.CustomerRepository;
import com.system.megacitycab.Booking.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/auth/bookings")
@Slf4j
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/getallbookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PostMapping("/createbooking")
    public ResponseEntity<Booking> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Validated @RequestBody BookingRequest bookingRequest) {
        String email = userDetails.getUsername();
        log.info("Create new booking for customer email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        bookingRequest.setCustomerId(customer.getCustomerId());
        Booking booking = bookingService.createBooking(bookingRequest);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String bookingId,
            @Validated @RequestBody CancellationRequest cancellationRequest) {
        String email = userDetails.getUsername();
        log.info("Cancelling booking: {} for customer email: {}", bookingId, email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        cancellationRequest.setBookingId(bookingId);
        Booking cancelledBooking = bookingService.cancelBooking(customer.getCustomerId(), cancellationRequest);
        return ResponseEntity.ok(cancelledBooking);
    }

    @GetMapping("/getallcustomerbookings")
    public ResponseEntity<List<Booking>> getCustomerBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        log.info("Fetching bookings for customer email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        List<Booking> bookings = bookingService.getCustomerBookings(customer.getCustomerId());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Booking> getCustomerBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String bookingId) {
        String email = userDetails.getUsername();
        log.info("Fetching booking: {} for customer email: {}", bookingId, email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        Booking booking = bookingService.getBookingDetails(customer.getCustomerId(), bookingId);
        return ResponseEntity.ok(booking);
    }

    @DeleteMapping("/delete/{bookingId}")
    public ResponseEntity<Void> deleteBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String bookingId) {
        String email = userDetails.getUsername();
        log.info("Deleting booking: {} for customer email: {}", bookingId, email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        bookingService.deleteBooking(customer.getCustomerId(), bookingId);
        return ResponseEntity.noContent().build();
    }
}