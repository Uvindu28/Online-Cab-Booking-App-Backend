package com.system.megacitycab.service;

import java.util.List;

import com.system.megacitycab.exception.ResourceNotFoundException;
import com.system.megacitycab.model.Booking;
import com.system.megacitycab.model.BookingStatus;
import com.system.megacitycab.repository.BookingRepository;
import com.system.megacitycab.repository.CarRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.system.megacitycab.model.Driver;
import com.system.megacitycab.repository.CustomerRepository;
import com.system.megacitycab.repository.DriverRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DriverServiceImpl implements DriverService{

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BookingRepository bookingRepository;

    public boolean isEmailTaken(String email) {
        return customerRepository.existsByEmail(email) || 
               driverRepository.existsByEmail(email);
    }

    @Override
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @Override
    public Driver getDriverById(String driverId) {
        return driverRepository.findById(driverId).orElse(null);
    }

    @Override
    public ResponseEntity<?> createDriver(Driver driver) {
        if (isEmailTaken(driver.getEmail())) {
            return ResponseEntity.badRequest()
                .body("Email already exists: " + driver.getEmail());
        }
        String encodedPassword = passwordEncoder.encode(driver.getPassword());
        driver.setPassword(encodedPassword);
        return ResponseEntity.ok(driverRepository.save(driver));
    }

    @Override
    public Driver updateDriver(String driverId, Driver driver) {
        log.info("Updating driver with ID: {}", driverId);

        return driverRepository.findById(driverId)
                .map(existingDriver -> {
                    existingDriver.setDriverName(driver.getDriverName());
                    existingDriver.setPhone(driver.getPhone());
                    existingDriver.setDriverLicense(driver.getDriverLicense());
                    Driver updatedDriver = driverRepository.save(existingDriver);
                    log.info("Successfully updated driver with ID: {}", updatedDriver.getDriverId());
                    return updatedDriver;
                })
                .orElseThrow(() -> {
                    log.error("Driver not found with ID: {}", driverId);
                    return new ResourceNotFoundException("Driver not found with id: " + driverId);
                });
    }

    @Override
    @Transactional
    public Driver updateAvailability(String driverId, boolean availability) {
        log.info("Updating availability to {} for driver: {}", availability, driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        if(!availability){
            List<Booking> activeBookings = bookingRepository.findByDriverId(driverId);
            boolean hasActiveBookings = activeBookings.stream()
                    .anyMatch(booking ->
                            booking.getStatus() == BookingStatus.CONFIRMED ||
                                    booking.getStatus() == BookingStatus.IN_PROGRESS
                    );
            if(hasActiveBookings){
                throw new IllegalStateException("Cannot update availability. Driver has active bookings");
            }
        }
        driver.setAvailable(availability);
        Driver updatedDriver = driverRepository.save(driver);
        log.info("Successfully updated availability for driver: {}", updatedDriver);
        return updatedDriver;
    }

    @Override
    public List<Booking> getDriverBookings(String driverId) {
        log.info("Fetching driver bookings for driver: {}", driverId);

        if(!driverRepository.existsById(driverId)){
            throw new ResourceNotFoundException("Driver not found with id: " + driverId);
        }

        return bookingRepository.findByDriverId(driverId);
    }

    @Override
    @Transactional
    public void deleteDriver(String driverId) {
        log.info("Attempting to delete driver with ID: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        List<Booking> activeBookings = bookingRepository.findByDriverId(driverId);
        boolean hasActiveBookings = activeBookings.stream()
                .anyMatch(booking ->
                        booking.getStatus() == BookingStatus.CONFIRMED ||
                        booking.getStatus() == BookingStatus.IN_PROGRESS
                );
        if(hasActiveBookings){
            throw new IllegalStateException("Cannot delete driver with active bookings");
        }

        if(driver.getCarId() != null){
            carRepository.findById(driver.getCarId()).ifPresent(car -> {
                car.setAssignedDriverId(null);
                carRepository.save(car);
            });
        }
        driverRepository.deleteById(driverId);
        log.info("Successfully deleted driver with ID: {}", driverId);
    }
}
