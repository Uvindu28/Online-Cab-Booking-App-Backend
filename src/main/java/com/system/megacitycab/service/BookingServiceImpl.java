package com.system.megacitycab.service;

import com.system.megacitycab.dto.BookingRequest;
import com.system.megacitycab.dto.CancellationRequest;
import com.system.megacitycab.exception.InvalidBookingException;
import com.system.megacitycab.exception.InvalidBookingStateException;
import com.system.megacitycab.exception.ResourceNotFoundException;
import com.system.megacitycab.exception.UnauthorizedException;
import com.system.megacitycab.model.*;
import com.system.megacitycab.repository.BookingRepository;
import com.system.megacitycab.repository.CarRepository;
import com.system.megacitycab.repository.CustomerRepository;
import com.system.megacitycab.repository.DriverRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private static final int CANCELLATION_WINDOW_HOURS = 24;
    private static final double CANCELLATION_FEE_PERCENTAGE = 0.1;

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }

    @Override
    @Transactional
    public Booking createBooking(BookingRequest request) {
        // Validate car availability
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found"));
        if (!isCarAvailableForTime(car, request.getPickupDate())) {
            throw new InvalidBookingException("Car is not available for requested time");
        }

        // Create booking
        Booking booking = new Booking();
        booking.setCustomerId(request.getCustomerId());
        booking.setCarId(request.getCarId());
        booking.setPickupLocation(request.getPickupLocation());
        booking.setDestination(request.getDestination());
        booking.setPickupDate(request.getPickupDate());
        booking.setBookingDate(LocalDateTime.now());
        booking.setDriverRequired(request.isDriverRequired());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalAmount(calculateBookingAmount(car, request));

        if (request.isDriverRequired()) {
            assignDriverToBooking(booking, car);
        }
        car.setAvailable(false);
        carRepository.save(car);

        log.info("Created new booking with ID: {} for customer: {}",
                booking.getBookingId(), booking.getCustomerId());
        return bookingRepository.save(booking);
    }

    private boolean isCarAvailableForTime(Car car, LocalDateTime requestedTime) {
        if (!car.isAvailable()) {
            return false;
        }
        List<Booking> existingBookings = bookingRepository.findByCarIdAndStatus(
                car.getCarId(),
                BookingStatus.CONFIRMED
        );
        return existingBookings.stream()
                .noneMatch(booking -> isTimeOverlapping(booking.getPickupDate(), requestedTime));
    }

    private boolean isTimeOverlapping(LocalDateTime existing, LocalDateTime requested) {
        Duration buffer = Duration.ofHours(1);
        return Math.abs(Duration.between(existing, requested).toHours()) < buffer.toHours();
    }

    private double calculateBookingAmount(Car car, BookingRequest request) {
        double baseAmount = car.getBaseRate();
        if (request.isDriverRequired()) {
            baseAmount += car.getDriverRate();
        }
        return baseAmount;
    }

    private void assignDriverToBooking(Booking booking, Car car) {
        Driver driver;
        if (car.getAssignedDriverId() != null) {
            driver = driverRepository.findById(car.getAssignedDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned driver not found"));
            if (!driver.isAvailable()) {
                throw new InvalidBookingException("Car's assigned driver is not available");
            }
        } else {
            driver = driverRepository.findFirstByAvailableAndHasOwnCarFalse(true)
                    .orElseThrow(() -> new ResourceNotFoundException("No available driver"));
        }

        booking.setDriverId(driver.getDriverId());
        driver.setAvailable(false);
        driverRepository.save(driver);
        log.info("Assigned driver {} to booking {}", driver.getDriverId(), booking.getBookingId());
    }

    @Transactional
    public Booking cancelBooking(String customerId, CancellationRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("Not authorized to cancel this booking");
        }

        if (!booking.canBeCancelled()) {
            throw new InvalidBookingStateException("Booking cannot be cancelled in current state");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getReason());
        booking.setCancellationTime(LocalDateTime.now());

        releaseBookingResource(booking);
        handleCancellationRefund(booking);

        log.info("Cancelled booking with ID: {} for customer: {}",
                booking.getBookingId(), booking.getCustomerId());
        return bookingRepository.save(booking);
    }

    private void releaseBookingResource(Booking booking) {
        if (booking.getCarId() != null) {
            Car car = carRepository.findById(booking.getCarId()).orElse(null);
            if (car != null && !car.isAvailable()) {
                car.setAvailable(true);
                carRepository.save(car);
                log.info("Released car {} from booking {}", car.getCarId(), booking.getBookingId());
            }
        }

        if (booking.getDriverId() != null) {
            Driver driver = driverRepository.findById(booking.getDriverId()).orElse(null);
            if (driver != null && !driver.isAvailable()) {
                driver.setAvailable(true);
                driverRepository.save(driver);
                log.info("Released driver {} from booking {}", driver.getDriverId(), booking.getBookingId());
            }
        }
    }

    private void handleCancellationRefund(Booking booking) {
        LocalDateTime cancellationDeadline = booking.getPickupDate()
                .minusHours(CANCELLATION_WINDOW_HOURS);
        if (LocalDateTime.now().isBefore(cancellationDeadline)) {
            booking.setRefundAmount(booking.getTotalAmount());
        } else {
            double cancellationFee = booking.getTotalAmount() * CANCELLATION_FEE_PERCENTAGE;
            booking.setRefundAmount(booking.getTotalAmount() - cancellationFee);
        }
        log.info("Processing refund of {} for booking {}",
                booking.getRefundAmount(), booking.getBookingId());
    }

    @Scheduled(fixedRate = 10000)
    public void checkAndUpdateCarAvailability() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> activeBookings = bookingRepository.findByStatusAndPickupDateBefore(
                BookingStatus.CONFIRMED, now);

        for (Booking booking : activeBookings) {
            updateBookingStatus(booking);
        }

        log.info("Completed periodic booking status check");
    }

    private void updateBookingStatus(Booking booking) {
        LocalDateTime pickupTime = booking.getPickupDate();
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(pickupTime)) {
            booking.setStatus(BookingStatus.IN_PROGRESS);
            bookingRepository.save(booking);
            log.info("Updated booking {} status to IN_PROGRESS", booking.getBookingId());
        }
    }

    @Transactional(readOnly = true)
    public List<Booking> getCustomerBookings(String customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Booking getBookingDetails(String customerId, String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("Not authorized to view this booking");
        }

        return booking;
    }
}




