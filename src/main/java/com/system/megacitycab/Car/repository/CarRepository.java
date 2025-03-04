package com.system.megacitycab.Car.repository;

import com.system.megacitycab.Car.model.Car;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends MongoRepository<Car, String> {
    List<Car> findByAvailable(boolean available);
    List<Car> findByAssignedDriverId(String driverId);
}
