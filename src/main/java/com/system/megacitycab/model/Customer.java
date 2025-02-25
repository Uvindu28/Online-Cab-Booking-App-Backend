package com.system.megacitycab.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "customers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    @Id
    private String customerId;
    
    private String name;

    private String email;
    
    private String address;
    
    private String nic;
    
    private String phone;

    private String password;

    private String role = "CUSTOMER";

    private String profileImage;
}
