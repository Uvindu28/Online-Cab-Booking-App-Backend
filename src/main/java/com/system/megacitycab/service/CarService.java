package com.system.megacitycab.service;

import com.system.megacitycab.model.Car;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CarService {
    List<Car> getAllCars();
    Car getCarById(String carId);
    Car createCar(Car car);
    Car updateCar(String carId, Car car);
    void deleteCar(String carId);
}
