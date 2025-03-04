package com.system.megacitycab.Car.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cars")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Car {
    @Id
    private String carId;

    private String licensePlate;

    private String model;

    private int numberOfSeats;

    private boolean available = true;

    private String carImageUrl;

    private String assignedDriverId;

    private double baseRate;

    private double driverRate;
}
