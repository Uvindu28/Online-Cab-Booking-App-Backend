package com.system.megacitycab.Customer.controller;

import java.io.IOException;
import java.util.List;

import com.system.megacitycab.Cloudinary.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.system.megacitycab.Customer.model.Customer;
import com.system.megacitycab.Customer.service.CustomerService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/auth/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/getallCustomers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/getcustomer/{customerId}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable String customerId) {
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/getcustomer/{customerId}/profileImage")
    public ResponseEntity<String> getCustomerProfileImage(@PathVariable String customerId) {
        try {
            Customer customer = customerService.getCustomerById(customerId);
            if (customer != null && customer.getProfileImage() != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(customer.getProfileImage());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching profile image: " + e.getMessage());
        }
    }

    @PostMapping("/createcustomer")
    public ResponseEntity<?> createCustomer(@RequestParam("name") String name,
                                            @RequestParam("email") String email,
                                            @RequestParam("address") String address,
                                            @RequestParam("nic") String nic,
                                            @RequestParam("phone") String phone,
                                            @RequestParam("password") String password,
                                            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            Customer customer = new Customer();
            customer.setName(name);
            customer.setEmail(email);
            customer.setAddress(address);
            customer.setNic(nic);
            customer.setPhone(phone);
            customer.setPassword(password);

            if (profileImage != null && !profileImage.isEmpty()) {
                String profileImageUrl = cloudinaryService.uploadImage(profileImage);
                customer.setProfileImage(profileImageUrl);
            }

            ResponseEntity<?> response = customerService.createCustomer(customer);
            return response;

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading image: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating customer: " + e.getMessage());
        }
    }

    @PutMapping("/updatecustomer/{customerId}")
    public ResponseEntity<?> updateCustomer(@PathVariable String customerId,
                                            @RequestParam("name") String name,
                                            @RequestParam("address") String address,
                                            @RequestParam("phone") String phone,
                                            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            // Create a customer object with updated details
            Customer customer = new Customer();
            customer.setName(name);
            customer.setAddress(address);
            customer.setPhone(phone);

            // Handle profile image update if provided
            if (profileImage != null && !profileImage.isEmpty()) {
                String profileImageUrl = cloudinaryService.uploadImage(profileImage);
                customer.setProfileImage(profileImageUrl);
            }

            // Call service to update customer
            Customer updatedCustomer = customerService.updateCustomer(customerId, customer);
            return ResponseEntity.ok(updatedCustomer);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading image: " + e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Customer not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating customer: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating customer: " + e.getMessage());
        }
    }
}