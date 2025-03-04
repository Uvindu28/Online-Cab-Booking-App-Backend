package com.system.megacitycab.Authentication.util;

import java.util.Date;
import java.util.function.Function;

import com.system.megacitycab.Admin.model.Admin;
import com.system.megacitycab.Customer.model.Customer;
import com.system.megacitycab.Driver.model.Driver;
import com.system.megacitycab.Admin.repository.AdminRepository;
import com.system.megacitycab.Customer.repository.CustomerRepository;
import com.system.megacitycab.Driver.repository.DriverRepository;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Value("${app.secret}")
    private String secret;
    @Autowired
    private DriverRepository driverRepository;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));  // Generate key with secret
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("USER");

        String userId = null;

        if (role.equals("ROLE_CUSTOMER")) {
            userId = customerRepository.findByEmail(userDetails.getUsername())
                    .map(Customer::getCustomerId).orElse(null);
        } else if (role.equals("ROLE_DRIVER")) {
            userId = driverRepository.findByEmail(userDetails.getUsername())
                    .map(Driver::getDriverId).orElse(null);
        } else if (role.equals("ROLE_ADMIN")) {
            userId = adminRepository.findByEmail(userDetails.getUsername())
                    .map(Admin::getAdminId).orElse(null);
        }

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("role", role)
                .claim("userId", userId)  // Store userId in token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 hours validity
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        final String userId = extractUserId(token);

        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}