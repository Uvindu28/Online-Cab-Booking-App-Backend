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
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/getcustomer/{customerId}")
    public Customer getCustomerById(@PathVariable String customerId) {
        return customerService.getCustomerById(customerId);
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

            return customerService.createCustomer(customer);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error uploading image: " + e.getMessage());
        }
    }

}
