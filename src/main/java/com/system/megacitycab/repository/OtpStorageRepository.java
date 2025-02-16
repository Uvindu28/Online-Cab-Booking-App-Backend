package com.system.megacitycab.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.system.megacitycab.model.OtpStorage;

public interface OtpStorageRepository extends MongoRepository<OtpStorage, String>{
    Optional<OtpStorage> findByEmail(String email);
    void deleteByEmail(String email);
    
}
