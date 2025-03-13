package com.system.megacitycab.Driver.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.system.megacitycab.Booking.model.Booking;
import com.system.megacitycab.Car.model.Car;
import com.system.megacitycab.Cloudinary.CloudinaryService;
import com.system.megacitycab.Customer.model.Customer;
import com.system.megacitycab.Customer.repository.CustomerRepository;
import com.system.megacitycab.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.system.megacitycab.Driver.model.Driver;
import com.system.megacitycab.Driver.service.DriverService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/auth/driver")
@Slf4j
public class DriverController {
    @Autowired
    private DriverService driverService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/getalldrivers")
    public List<Driver> getAllDrivers() {
        return driverService.getAllDrivers();
    }

    @GetMapping("/getdriver/{driverId}")
    public Driver getDriverById(@PathVariable String driverId) {
        return driverService.getDriverById(driverId); // Ensure Car is included in the response
    }

    @PostMapping(value = "/createdriver",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createDriver(
            @RequestParam("driverName") String driverName,
            @RequestParam("email") String email,
            @RequestParam("driverLicense") String driverLicense,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password,
            @RequestParam("hasOwnCar") boolean hasOwnCar,
            @RequestParam(value = "licensePlate", required = false) String licensePlate,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "numberOfSeats", required = false) Integer numberOfSeats,
            @RequestParam(value = "baseRate", required = false) Double baseRate,
            @RequestParam(value = "driverRate", required = false) Double driverRate,
            @RequestParam(value = "carImage", required = false) MultipartFile carImage,
            @RequestParam(value = "profileImage") MultipartFile profileImage) {

        try {
            Driver driver = new Driver();
            driver.setDriverName(driverName);
            driver.setEmail(email);
            driver.setDriverLicense(driverLicense);
            driver.setPhone(phone);
            driver.setPassword(password);
            driver.setHasOwnCar(hasOwnCar);

            if (profileImage != null && !profileImage.isEmpty()) {
                String profileImageUrl = cloudinaryService.uploadImage(profileImage);
                driver.setProfileImage(profileImageUrl);
            }

            Car car = null;
            if (hasOwnCar) {
                car = new Car();
                car.setLicensePlate(licensePlate);
                car.setModel(model);
                car.setNumberOfSeats(numberOfSeats != null ? numberOfSeats : 4);
                car.setBaseRate(baseRate != null ? baseRate : 0.0);
                car.setDriverRate(driverRate != null ? driverRate : 0.0);
                if (carImage != null && !carImage.isEmpty()) {
                    String carImageUrl = cloudinaryService.uploadImage(carImage);
                    car.setCarImageUrl(carImageUrl);
                }
            }

            return driverService.createDriver(driver, car);

        } catch (Exception e) {
            log.error("Error creating driver: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating driver: " + e.getMessage());
        }
    }

    @PutMapping("/updatedriver/{driverId}")
    public ResponseEntity<Driver> updateDriver(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String driverId,
            @RequestBody Driver driver) {
        String email = userDetails.getUsername();
        log.info("Updating driver with ID: {} for email: {}", driverId, email);

        Driver updatedDriver = driverService.updateDriver(driverId, driver);
        return ResponseEntity.ok(updatedDriver);
    }

    @PutMapping("/{driverId}/availability")
    public ResponseEntity<Driver> updateAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String driverId,
            @RequestBody Map<String, Boolean> availability) {
        String email = userDetails.getUsername();
        log.info("Updating availability for driver: {} for email: {}", driverId, email);

        if (!availability.containsKey("availability")) {
            return ResponseEntity.badRequest().build();
        }

        Driver driver = driverService.updateAvailability(driverId, availability.get("availability"));
        return ResponseEntity.ok(driver);
    }

    @GetMapping("/{driverId}/bookings")
    public ResponseEntity<List<Booking>> getDriverBookings(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String driverId) {
        String email = userDetails.getUsername();
        log.info("Fetching bookings for driver: {} for email: {}", driverId, email);

        Driver driver = driverService.getDriverById(driverId);
        if (!email.equals(driver.getEmail())) {
            log.warn("Unauthorized access attempt by user: {}", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Booking> bookings = driverService.getDriverBookings(driverId).stream()
                .map(booking -> {
                    Customer customer = customerRepository.findById(booking.getCustomerId())
                            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
                    booking.setPassengerName(customer.getName());
                    booking.setPassengerImage(customer.getProfileImage());
                    booking.setPassengerRating(customer.getRating() != null ? customer.getRating() : 0.0);
                    return booking;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{driverId}")
    public ResponseEntity<Void> deleteDriver(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String driverId) {
        String email = userDetails.getUsername();
        log.info("Deleting driver with ID: {} for email: {}", driverId, email);

        driverService.deleteDriver(driverId);
        return ResponseEntity.noContent().build();
    }
}