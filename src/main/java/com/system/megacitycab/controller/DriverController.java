package com.system.megacitycab.controller;

import java.util.List;
import java.util.Map;

import com.system.megacitycab.exception.UnauthorizedException;
import com.system.megacitycab.model.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.system.megacitycab.model.Driver;
import com.system.megacitycab.service.DriverService;

import javax.swing.text.html.parser.Entity;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/auth/driver")
@Slf4j
public class DriverController {
    @Autowired
    private DriverService driverService;

    @GetMapping("/getalldrivers")
    public List<Driver> getAllDrivers() {
        return driverService.getAllDrivers();
    }

    @GetMapping("/getdriver/{id}")
    public Driver getDriverById(@PathVariable String driverId) {
        return driverService.getDriverById(driverId);
    }

    @PostMapping("/createdriver")
    public ResponseEntity<?> createDriver(@RequestBody Driver driver) {
        return driverService.createDriver(driver);
    }

    @PutMapping("/updatedriver/{driverId}")
    public ResponseEntity<Driver> updateDriver(
            @PathVariable String driverId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Driver driver){
        log.info("Updating driver with ID: {}", driverId);
        validateDriverAuthorization(userDetails, driverId);
        driver.setDriverId(driverId);
        Driver updateDriver = driverService.updateDriver(driverId, driver);
        return ResponseEntity.ok(updateDriver);

    }

    @PutMapping("/{driverId}/availability")
    public ResponseEntity<Driver> updateAvailability(
            @PathVariable String driverId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Boolean> availability){
        log.info("Updating availability for driver: {}", driverId);
        validateDriverAuthorization(userDetails, driverId);
        Driver driver = driverService.updateAvailability(driverId, availability.get("availability"));
        return ResponseEntity.ok(driver);
    }

    @GetMapping("/{driverId}/bookings")
    public ResponseEntity<List<Booking>> getDriverBookings(
            @PathVariable String driverId,
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("Fetching bookings for driver: {}", driverId);
        validateDriverAuthorization(userDetails, driverId);
        List<Booking> bookings = driverService.getDriverBookings(driverId);
        return ResponseEntity.ok(bookings);

    }

    @DeleteMapping("/{driverId}")
    public ResponseEntity<Void> deleteDriver(
            @PathVariable String driverId,
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("Deleting driver with ID: {}", driverId);
        validateDriverAuthorization(userDetails, driverId);
        driverService.deleteDriver(driverId);
        return ResponseEntity.noContent().build();
    }

    public void validateDriverAuthorization(UserDetails userDetails, String driverId){
        if(!userDetails.getUsername().equals(driverId)){
            log.info("Authorization failed: authenticated user {} does not match driver ID {}",
                    userDetails.getUsername(), driverId);
            throw new UnauthorizedException("Not authorized to perform this action");
        }
    }
}
