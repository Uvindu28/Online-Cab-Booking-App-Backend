package com.system.megacitycab.Driver.service;

import java.util.List;

import com.system.megacitycab.Booking.model.Booking;
import com.system.megacitycab.Car.model.Car;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.system.megacitycab.Driver.model.Driver;

@Service
public interface DriverService {
    List<Driver> getAllDrivers();
    Driver getDriverById(String driverId);
    ResponseEntity<?> createDriver(Driver driver, Car car);
    Driver updateDriver(String driverId, Driver driver);
    Driver updateAvailability(String driverId, boolean availability);
    List<Booking> getDriverBookings(String driverId);
    void deleteDriver(String driverId);

    
}
