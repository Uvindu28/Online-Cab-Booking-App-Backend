package com.system.megacitycab.Car.controller;

import com.system.megacitycab.Car.model.Car;
import com.system.megacitycab.Car.service.CarService;
import com.system.megacitycab.Cloudinary.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5175")
public class CarController {

    @Autowired
    private CarService carService;

    @Autowired
    private CloudinaryService cloudinaryService;


    @GetMapping("/all/getallcars")
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/all/getcarbyid/{carId}")
    public Car getCarById(@PathVariable String carId) {
        return carService.getCarById(carId);
    }

    @PostMapping("/auth/cars/createcar")
    public ResponseEntity<Car> createCar(@RequestParam String licensePlate,
                                         @RequestParam String model,
                                         @RequestParam int numberOfSeats,
                                         @RequestParam MultipartFile carImage) throws IOException {
        String imageUrl = cloudinaryService.uploadImage(carImage);

        Car car = new Car();
        car.setLicensePlate(licensePlate);
        car.setModel(model);
        car.setNumberOfSeats(numberOfSeats);
        car.setCarImageUrl(imageUrl);

        Car savedCar = carService.createCar(car);
        return ResponseEntity.ok(savedCar);
    }

    @PutMapping("/auth/cars/updatecar/{carId}")
    public ResponseEntity<Car> updateCar(@PathVariable("carId") String carId, @RequestBody Car car){
        Car updateCar = carService.updateCar(carId, car);
        return ResponseEntity.ok(updateCar);
    }

    @DeleteMapping("/auth/cars/deletecar/{carId}")
    public void deleteCar(@PathVariable("carId") String carId){
        carService.deleteCar(carId);
    }
}
