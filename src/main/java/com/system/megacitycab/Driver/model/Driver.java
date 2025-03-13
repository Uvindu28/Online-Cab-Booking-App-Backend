package com.system.megacitycab.Driver.model;

import com.system.megacitycab.Car.model.Car;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "drivers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Driver {
    @Id
    private String driverId;

    private String driverName;

    private String email;

    private String driverLicense;

    private String phone;

    private String password;

    private String profileImage;

    private boolean hasOwnCar;

    private String carId; // Reference to the driver's car

    private boolean available = true; // For availability status (used in /availability endpoint)

    private String role = "DRIVER";
}