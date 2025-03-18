package com.system.megacitycab.Booking.serviceImpl;

import com.system.megacitycab.Authentication.service.EmailService;
import com.system.megacitycab.Booking.Enum.BookingStatus;
import com.system.megacitycab.Booking.model.Booking;
import com.system.megacitycab.Booking.dto.BookingRequest;
import com.system.megacitycab.Booking.dto.CancellationRequest;
import com.system.megacitycab.Car.model.Car;
import com.system.megacitycab.Customer.model.Customer;
import com.system.megacitycab.Driver.model.Driver;
import com.system.megacitycab.Driver.service.DriverService;
import com.system.megacitycab.exception.InvalidBookingException;
import com.system.megacitycab.exception.InvalidBookingStateException;
import com.system.megacitycab.exception.ResourceNotFoundException;
import com.system.megacitycab.exception.UnauthorizedException;
import com.system.megacitycab.Booking.repository.BookingRepository;
import com.system.megacitycab.Car.repository.CarRepository;
import com.system.megacitycab.Customer.repository.CustomerRepository;
import com.system.megacitycab.Driver.repository.DriverRepository;
import com.system.megacitycab.Booking.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DriverService driverService;

    private static final int CANCELLATION_WINDOW_HOURS = 24;
    private static final double CANCELLATION_FEE_PERCENTAGE = 0.1;

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }

    @Override
    @Transactional
    public Booking createBooking(BookingRequest request) {
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found"));
        if (!isCarAvailableForTime(car, request.getPickupDate())) {
            throw new InvalidBookingException("Car is not available for requested time");
        }

        Booking booking = new Booking();
        booking.setCustomerId(request.getCustomerId());
        booking.setCarId(request.getCarId());
        booking.setBookingId(request.getBookingId());
        booking.setPickupLocation(request.getPickupLocation());
        booking.setDestination(request.getDestination());
        booking.setPickupDate(request.getPickupDate());
        booking.setPickupTime(request.getPickupTime());
        booking.setBookingDate(LocalDateTime.now().format(DATE_FORMATTER));
        booking.setDriverRequired(request.isDriverRequired());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalAmount(calculateBookingAmount(car, request));

        if (request.isDriverRequired()) {
            assignDriverToBooking(booking, car);
        }
        carRepository.save(car);

        Booking savedBooking = bookingRepository.save(booking);

        if (booking.isDriverRequired()) {
            Driver driver = driverService.getDriverById(booking.getDriverId());
            savedBooking.setDriverDetails(driver);
        }

        // Send email confirmation to customer
        sendBookingConfirmationEmail(savedBooking);

        log.info("Created new booking with ID: {} for customer: {}",
                booking.getBookingId(), booking.getCustomerId());
        return savedBooking;
    }

    @Override
    public Booking confirmBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking can only be confirmed from PENDING status.");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking confirmedBooking = bookingRepository.save(booking);

        // Send confirmation email when booking is confirmed
        sendBookingStatusUpdateEmail(confirmedBooking, "Booking Confirmed");

        return confirmedBooking;
    }



    @Override
    public void deleteBooking(String customerId, String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("Not authorized to delete this booking");
        }

        if (!booking.canBeDeleted()) {
            throw new InvalidBookingStateException("Booking cannot be deleted in current state");
        }

        releaseBookingResource(booking);
        bookingRepository.delete(booking);
        log.info("Deleted booking with ID: {} for customer: {}", bookingId, customerId);
    }
    public boolean hasBookingWithDriver(String customerEmail, String driverId) {
        return bookingRepository.existsByCustomerEmailAndDriverId(customerEmail, driverId);
    }

    private boolean isCarAvailableForTime(Car car, String requestedTime) {
        if (!car.isAvailable()) {
            return false;
        }
        List<Booking> existingBookings = bookingRepository.findByCarIdAndStatus(
                car.getCarId(),
                BookingStatus.CONFIRMED
        );
        return existingBookings.stream()
                .noneMatch(booking -> isTimeOverlapping(booking.getPickupDate(), requestedTime));
    }

    private boolean isTimeOverlapping(String existing, String requested) {
        LocalDateTime existingTime = parsePickupDate(existing);
        LocalDateTime requestedTime = parsePickupDate(requested);
        Duration buffer = Duration.ofHours(1);
        return Math.abs(Duration.between(existingTime, requestedTime).toHours()) < buffer.toHours();
    }

    private double calculateBookingAmount(Car car, BookingRequest request) {
        double baseAmount = car.getBaseRate();
        if (request.isDriverRequired()) {
            baseAmount += car.getDriverRate();
        }
        return baseAmount;
    }

    private void assignDriverToBooking(Booking booking, Car car) {
        Driver driver;
        if (car.getAssignedDriverId() != null) {
            driver = driverRepository.findById(car.getAssignedDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned driver not found"));
            if (!driver.isAvailable()) {
                throw new InvalidBookingException("Car's assigned driver is not available");
            }
        } else {
            driver = driverRepository.findFirstByAvailableAndHasOwnCarFalse(true)
                    .orElseThrow(() -> new ResourceNotFoundException("No available driver"));
        }

        booking.setDriverId(driver.getDriverId());
        driver.setAvailable(false);
        driverRepository.save(driver);
        log.info("Assigned driver {} to booking {}", driver.getDriverId(), booking.getBookingId());
    }

    @Override
    @Transactional
    public Booking cancelBooking(String customerId, CancellationRequest request) {
        log.info("Cancelling booking with ID: {} for customer: {}", request.getBookingId(), customerId);

        if (request.getBookingId() == null || request.getBookingId().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be null or empty");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> {
                    log.error("Booking not found with ID: {}", request.getBookingId());
                    return new ResourceNotFoundException("Booking not found or already deleted");
                });

        if (!booking.getCustomerId().equals(customerId)) {
            log.warn("Unauthorized cancellation attempt for booking: {} by customer: {}", request.getBookingId(), customerId);
            throw new UnauthorizedException("Not authorized to cancel this booking");
        }

        if (!booking.canBeCancelled()) {
            log.warn("Invalid cancellation attempt for booking: {} in state: {}", request.getBookingId(), booking.getStatus());
            throw new InvalidBookingStateException("Booking cannot be cancelled in current state");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getReason());
        booking.setCancellationTime(LocalDateTime.now().format(DATE_FORMATTER));

        releaseBookingResource(booking);
        handleCancellationRefund(booking);

        bookingRepository.save(booking);

        // Send cancellation email
        sendBookingStatusUpdateEmail(booking, "Booking Cancelled");

        log.info("Successfully cancelled booking with ID: {} for customer: {}", booking.getBookingId(), booking.getCustomerId());
        return booking;
    }

    private void releaseBookingResource(Booking booking) {
        if (booking.getCarId() != null) {
            Car car = carRepository.findById(booking.getCarId()).orElse(null);
            if (car != null && !car.isAvailable()) {
                car.setAvailable(true);
                carRepository.save(car);
                log.info("Released car {} from booking {}", car.getCarId(), booking.getBookingId());
            }
        }

        if (booking.getDriverId() != null) {
            Driver driver = driverRepository.findById(booking.getDriverId()).orElse(null);
            if (driver != null && !driver.isAvailable()) {
                driver.setAvailable(true);
                driverRepository.save(driver);
                log.info("Released driver {} from booking {}", driver.getDriverId(), booking.getBookingId());
            }
        }
    }

    private void handleCancellationRefund(Booking booking) {
        LocalDateTime pickupDateTime = parsePickupDate(booking.getPickupDate());
        LocalDateTime cancellationDeadline = pickupDateTime.minusHours(CANCELLATION_WINDOW_HOURS);
        if (LocalDateTime.now().isBefore(cancellationDeadline)) {
            booking.setRefundAmount(booking.getTotalAmount());
        } else {
            double cancellationFee = booking.getTotalAmount() * CANCELLATION_FEE_PERCENTAGE;
            booking.setRefundAmount(booking.getTotalAmount() - cancellationFee);
        }
        log.info("Processing refund of {} for booking {}", booking.getRefundAmount(), booking.getBookingId());
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void checkAndUpdateCarAvailability() {
        ZoneId sriLankaZoneId = ZoneId.of("Asia/Colombo");
        LocalDateTime now = LocalDateTime.now(sriLankaZoneId);
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime().truncatedTo(ChronoUnit.SECONDS);

        List<Booking> activeBookings = bookingRepository.findByStatusAndPickupDateBefore(
                BookingStatus.CONFIRMED, now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        for (Booking booking : activeBookings) {
            LocalDateTime pickupDateTime = parsePickupDate(booking.getPickupDate());
            if (pickupDateTime.isBefore(now)) {
                String carId = booking.getCarId();
                Optional<Car> car = carRepository.findById(carId);
                car.ifPresent(c -> {
                    c.setAvailable(false);
                    carRepository.save(c);
                });

                updateBookingStatus(booking);
            }
        }

        log.info("Completed periodic booking status check at {}", now);
    }

    private void updateBookingStatus(Booking booking) {
        try {
            LocalDateTime pickupTime = parsePickupDate(booking.getPickupDate());
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(pickupTime)) {
                booking.setStatus(BookingStatus.IN_PROGRESS);
                bookingRepository.save(booking);
                log.info("Updated booking {} status to IN_PROGRESS", booking.getBookingId());
            }
        } catch (DateTimeParseException e) {
            log.error("Failed to parse pickup date for booking {}: {}",
                    booking.getBookingId(), booking.getPickupDate(), e);
        }
    }

    private LocalDateTime parsePickupDate(String pickupDate) {
        try {
            return LocalDateTime.parse(pickupDate, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                LocalDate date = LocalDate.parse(pickupDate, DATE_ONLY_FORMATTER);
                return date.atStartOfDay();
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Invalid date format: " + pickupDate, e2);
            }
        }
    }
    private void sendBookingConfirmationEmail(Booking booking) {
        try {
            Customer customer = customerRepository.findById(booking.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            Car car = carRepository.findById(booking.getCarId())
                    .orElseThrow(() -> new ResourceNotFoundException("Car not found"));

            String subject = "MegaCityCab - Booking Confirmation #" + booking.getBookingId();
            String emailBody = generateBookingEmailBody(booking, customer, car);

            emailService.sendHtmlEmail(customer.getEmail(), subject, emailBody);
            log.info("Booking confirmation email sent to customer: {}", customer.getEmail());
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email: {}", e.getMessage(), e);
        }
    }

    private void sendBookingStatusUpdateEmail(Booking booking, String statusMessage) {
        try {
            Customer customer = customerRepository.findById(booking.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            String subject = "MegaCityCab - " + statusMessage + " #" + booking.getBookingId();
            StringBuilder emailBody = new StringBuilder();

            emailBody.append("Dear ").append(customer.getName()).append(",\n\n");
            emailBody.append("Your booking with ID: ").append(booking.getBookingId()).append(" has been ").append(statusMessage.toLowerCase()).append(".\n\n");

            if (booking.getStatus() == BookingStatus.CANCELLED && booking.getRefundAmount() > 0) {
                emailBody.append("A refund of $").append(booking.getRefundAmount()).append(" will be processed to your original payment method.\n\n");
            }

            emailBody.append("If you have any questions, please contact our customer service team.\n");
            emailBody.append("Phone: +94 11 123 4567\n");
            emailBody.append("Email: support@megacitycab.com\n\n");

            emailBody.append("Thank you for choosing MegaCityCab!\n");

            emailService.sendHtmlEmail(customer.getEmail(), subject, emailBody.toString());
            log.info("Booking status update email sent to customer: {}", customer.getEmail());
        } catch (Exception e) {
            log.error("Failed to send booking status update email: {}", e.getMessage(), e);
        }
    }

    private String generateBookingEmailBody(Booking booking, Customer customer, Car car) {
        StringBuilder emailBody = new StringBuilder();

        emailBody.append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }")
                .append(".container { width: 100%; padding: 20px; }")
                .append(".content { max-width: 600px; margin: auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }")
                .append(".header { text-align: center; padding: 10px 0; }")
                .append(".header img { width: 150px; }")
                .append(".details { margin: 20px 0; }")
                .append(".details h2 { background-color: #007BFF; color: #ffffff; padding: 10px; border-radius: 5px; }")
                .append(".details p { margin: 5px 0; }")
                .append(".invoice { background-color: #f9f9f9; padding: 20px; border-radius: 10px; }")
                .append(".invoice table { width: 100%; border-collapse: collapse; }")
                .append(".invoice th, .invoice td { padding: 10px; border-bottom: 1px solid #dddddd; }")
                .append(".invoice th { text-align: left; background-color: #007BFF; color: #ffffff; }")
                .append(".total { text-align: right; font-weight: bold; }")
                .append(".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #888888; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='content'>")
                .append("<div class='header'>")
                .append("<img src='https://www.megacitycab.com/logo.png' alt='MegaCityCab Logo' />")
                .append("<h1>Booking Confirmation</h1>")
                .append("</div>")
                .append("<p>Dear ").append(customer.getName()).append(",</p>")
                .append("<p>Thank you for choosing MegaCityCab. Your booking has been confirmed with the following details:</p>")
                .append("<div class='details'>")
                .append("<h2>Booking Details</h2>")
                .append("<p><strong>Booking ID:</strong> ").append(booking.getBookingId()).append("</p>")
                .append("<p><strong>Booking Date:</strong> ").append(booking.getBookingDate()).append("</p>")
                .append("<p><strong>Pickup Location:</strong> ").append(booking.getPickupLocation()).append("</p>")
                .append("<p><strong>Destination:</strong> ").append(booking.getDestination()).append("</p>")
                .append("<p><strong>Pickup Date:</strong> ").append(booking.getPickupDate()).append("</p>")
                .append("<p><strong>Pickup Time:</strong> ").append(booking.getPickupTime()).append("</p>")
                .append("</div>")
                .append("<div class='details'>")
                .append("<h2>Vehicle Details</h2>")
                .append("<p><strong>Car Model:</strong> ").append(car.getModel()).append("</p>")
                .append("<p><strong>License Plate:</strong> ").append(car.getLicensePlate()).append("</p>")
                .append("<p><strong>Driver Required:</strong> ").append(booking.isDriverRequired() ? "Yes" : "No").append("</p>")
                .append("</div>")
                .append("<div class='invoice'>")
                .append("<h2>Payment Details</h2>")
                .append("<table>")
                .append("<tr><th>Description</th><th>Amount</th></tr>")
                .append("<tr><td>Base Rate</td><td>$").append(car.getBaseRate()).append("</td></tr>");
        if (booking.isDriverRequired()) {
            emailBody.append("<tr><td>Driver Rate</td><td>$").append(car.getDriverRate()).append("</td></tr>");
        }
        emailBody.append("<tr><td class='total'>Total Amount</td><td class='total'>$").append(booking.getTotalAmount()).append("</td></tr>")
                .append("</table>")
                .append("</div>")
                .append("<div class='details'>")
                .append("<h2>Cancellation Policy</h2>")
                .append("<p>- Cancellations made more than 24 hours before the pickup time will receive a full refund.</p>")
                .append("<p>- Cancellations made within 24 hours of the pickup time will incur a 10% cancellation fee.</p>")
                .append("</div>")
                .append("<p>If you have any questions or need to make changes to your booking, please contact our customer service team.</p>")
                .append("<p><strong>Phone:</strong> +94 11 123 4567</p>")
                .append("<p><strong>Email:</strong> support@megacitycab.com</p>")
                .append("<p>Thank you for choosing MegaCityCab!</p>")
                .append("<div class='footer'>")
                .append("<p>&copy; 2025 MegaCityCab. All rights reserved.</p>")
                .append("</div>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return emailBody.toString();
    }


    @Transactional(readOnly = true)
    public List<Booking> getCustomerBookings(String customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Booking getBookingDetails(String customerId, String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("Not authorized to view this booking");
        }

        return booking;
    }

    @Override
    public List<Booking> getAvailableBookings() {
        // Return bookings that are PENDING and have no assigned driver
        return bookingRepository.findByDriverIdIsNullAndStatus(BookingStatus.PENDING);
    }
}