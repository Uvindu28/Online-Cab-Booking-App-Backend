package com.system.megacitycab.serviceImpl;

import com.system.megacitycab.model.Car;
import com.system.megacitycab.repository.CarRepository;
import com.system.megacitycab.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarServiceImpl implements CarService {

    @Autowired
    private CarRepository carRepository;

    @Override
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    @Override
    public Car getCarById(String carId) {
        return carRepository.findById(carId).orElse(null);
    }

    @Override
    public Car createCar(Car car) {
        return carRepository.save(car);
    }

    @Override
    public Car updateCar(String carId, Car car) {
        return carRepository.findById(carId)
                .map(existCar ->{
                    existCar.setModel(car.getModel());
                    existCar.setAvailable(car.isAvailable());
                    existCar.setLicensePlate(car.getLicensePlate());
                    existCar.setNumberOfSeats(car.getNumberOfSeats());
                    existCar.setCarImageUrl(car.getCarImageUrl());
                    return carRepository.save(existCar);
                })
                .orElseThrow(() -> new RuntimeException("Car not found"));
    }

    @Override
    public void deleteCar(String carId) {
        carRepository.deleteById(carId);

    }
}
