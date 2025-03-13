package com.system.megacitycab.Authentication.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.system.megacitycab.Authentication.model.OtpStorage;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpStorageRepository extends MongoRepository<OtpStorage, String>{
    Optional<OtpStorage> findByEmail(String email);
    void deleteByEmail(String email);
    
}
