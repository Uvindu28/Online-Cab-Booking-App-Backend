package com.system.megacitycab.Driver.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.system.megacitycab.Booking.model.Booking;
import com.system.megacitycab.Car.model.Car;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.system.megacitycab.Driver.model.Driver;
import com.system.megacitycab.Driver.service.DriverService;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/getdriver/{driverId}")
    public Driver getDriverById(@PathVariable String driverId) {
        return driverService.getDriverById(driverId);
    }

    @GetMapping("/getdriver/{customerId}/profileImage")
    public ResponseEntity<String> getDriverProfileImage(@PathVariable String driverId) {
        Driver driver = driverService.getDriverById(driverId);
        if (driver != null && driver.getProfileImage() != null) {
            return ResponseEntity.ok(driver.getProfileImage());
        }
        return ResponseEntity.notFound().build();
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
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        try{
            Driver driver = new Driver();
            driver.setDriverName(driverName);
            driver.setEmail(email);
            driver.setDriverLicense(driverLicense);
            driver.setPhone(phone);
            driver.setPassword(password);
            driver.setHasOwnCar(hasOwnCar);

            if (profileImage != null && !profileImage.isEmpty()) {
                String profileImageUrl = handleImageUpload(profileImage, "driver");
                driver.setProfileImage(profileImageUrl);
            }

            Car car = null;
            if(hasOwnCar){
                car = new Car();
                car.setLicensePlate(licensePlate);
                car.setModel(model);

                if (numberOfSeats != null) {
                    car.setNumberOfSeats(numberOfSeats);
                } else {
                    car.setNumberOfSeats(4);
                }

                if (baseRate != null) {
                    car.setBaseRate(baseRate);
                }

                if (driverRate != null) {
                    car.setDriverRate(driverRate);
                }

                if(carImage != null && !carImage.isEmpty()){
                    String carImageUrl = handleImageUpload(carImage, "car");
                    car.setCarImageUrl(carImageUrl);
                }
            }
            //

            return driverService.createDriver(driver, car);

        }catch (Exception e){
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

    @PutMapping("/{driverId}/uploadProfileImage")
    public ResponseEntity<?> uploadProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String driverId,
            @RequestParam("profileImage") MultipartFile profileImage) {
        try {
            String email = userDetails.getUsername();
            log.info("Uploading profile image for driver: {} (User: {})", driverId, email);

            Optional<Driver> driverOpt = Optional.ofNullable(driverService.getDriverById(driverId));
            if (!driverOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Driver not found");
            }

            Driver driver = driverOpt.get();
            if (profileImage != null && !profileImage.isEmpty()) {
                String profileImageUrl = handleImageUpload(profileImage, "driver");
                driver.setProfileImage(profileImageUrl);
                driverService.updateDriver(driverId, driver);
            }

            return ResponseEntity.ok("Profile image uploaded successfully");

        } catch (Exception e) {
            log.error("Error uploading profile image: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading profile image: " + e.getMessage());
        }
    }

    @GetMapping("/{driverId}/bookings")
    public ResponseEntity<List<Booking>> getDriverBookings(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String driverId) {
        String email = userDetails.getUsername();
        log.info("Fetching bookings for driver: {} for email: {}", driverId, email);

        List<Booking> bookings = driverService.getDriverBookings(driverId);
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

    private String handleImageUpload(MultipartFile file, String type) throws IOException {
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        String basePath = type.equals("driver") ? "drivers/" : "cars/";
        String uploadDir = "uploads/" + basePath;

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Path filePath = Paths.get(uploadDir + filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return basePath + filename;
    }

}
