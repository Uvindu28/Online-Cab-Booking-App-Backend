package com.system.megacitycab.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {
    @Id
    private String driverId;

    private String driverName;

    private String email;

    private String phone;

    private String driverLicense;

    private String password;

    private boolean available = true;

    private String role = "DRIVER";

    private String carId;

    private boolean hasOwnCar = false;

}
