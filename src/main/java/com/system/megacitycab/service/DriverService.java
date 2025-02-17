package com.system.megacitycab.service;

import java.util.List;

import com.system.megacitycab.model.Booking;
import com.system.megacitycab.model.Car;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.system.megacitycab.model.Driver;

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
