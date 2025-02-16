package com.system.megacitycab.repository;

import com.system.megacitycab.model.Car;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends MongoRepository<Car, String> {
    List<Car> findByAvailable(boolean available);
    List<Car> findByAssignedDriverId(String driverId);
}
