package com.system.megacitycab.controller;

import com.system.megacitycab.model.Car;
import com.system.megacitycab.service.CarService;
import com.system.megacitycab.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class CarController {

    @Autowired
    private CarService carService;

    @Autowired
    private CloudinaryService cloudinaryService;


    @GetMapping("/all/getallCars")
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/all/getcarbyid/{carId}")
    public Car getCarById(@PathVariable String carId) {
        return carService.getCarById(carId);
    }

    @PostMapping("/cars/createcar")
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

    @PutMapping("/cars/updatecar/{carId}")
    public ResponseEntity<Car> updateCar(@PathVariable("carId") String carId, @RequestBody Car car){
        Car updateCar = carService.updateCar(carId, car);
        return ResponseEntity.ok(updateCar);
    }

    @DeleteMapping("/cars/deletecar/{carId}")
    public void deleteCar(@PathVariable("carId") String carId){
        carService.deleteCar(carId);
    }
}
