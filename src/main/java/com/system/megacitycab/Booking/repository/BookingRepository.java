package com.system.megacitycab.Booking.repository;

import com.system.megacitycab.Booking.model.Booking;
import com.system.megacitycab.Booking.Enum.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByCustomerId(String customerId);
    List<Booking> findByDriverId(String driverId);
    List<Booking> findByStatusAndPickupDateBefore(BookingStatus status, String dateTime);
    List<Booking> findByCarIdAndStatus(String carId, BookingStatus status);
    List<Booking> findByDriverIdIsNullAndStatus(BookingStatus status);
    boolean existsByCustomerEmailAndDriverId(String customerEmail, String driverId);

    // Add this new method for finding bookings by multiple statuses
    List<Booking> findByStatusIn(List<BookingStatus> statuses);

    // Add this method to find bookings by status and check both date and time
    @Query("{'status': {$in: ?0}, $or: [{'pickupDate': {$lt: ?1}}, {'pickupDate': ?1, 'pickupTime': {$lt: ?2}}]}")
    List<Booking> findBookingsWithPassedPickupTime(List<String> statuses, String currentDate, String currentTime);

    @Query("{'carId': ?0, 'pickupDate': {$gte: ?1, $lte: ?2}, 'status': {$in: ['CONFIRMED','IN_PROGRESS']}}")
    List<Booking> findOverlappingBookings(String carId, LocalDateTime start, LocalDateTime end);
}
