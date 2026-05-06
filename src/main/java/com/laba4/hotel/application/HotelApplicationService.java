package com.laba4.hotel.application;

import com.laba4.hotel.data.InMemoryHotelDatabase;
import com.laba4.hotel.domain.Booking;
import com.laba4.hotel.domain.HotelException;
import com.laba4.hotel.domain.Room;
import com.laba4.hotel.domain.RoomType;
import com.laba4.hotel.domain.ServiceItem;
import com.laba4.hotel.domain.ServiceRequest;
import com.laba4.hotel.domain.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HotelApplicationService {
    public record TokenDetails(String accessToken, long expiresIn) {}

    private static final long TOKEN_EXPIRES_IN_SECONDS = 3600;

    private final InMemoryHotelDatabase db;
    private final Map<String, TokenRecord> tokenByValue = new ConcurrentHashMap<>();

    public HotelApplicationService(InMemoryHotelDatabase db) {
        this.db = db;
    }

    private static class TokenRecord {
        private final Long userId;
        private final Instant expiresAt;

        private TokenRecord(Long userId, Instant expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }

    public User register(String email, String password, String fullName, String phone) {
        if (email == null || !email.contains("@")) {
            throw new HotelException(400, "INVALID_EMAIL", "Invalid email");
        }
        if (password == null || password.length() < 6) {
            throw new HotelException(400, "INVALID_PASSWORD", "Invalid password");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new HotelException(400, "INVALID_FULL_NAME", "fullName is required");
        }

        Optional<User> existing = db.findUserByEmail(email);
        if (existing.isPresent()) {
            throw new HotelException(409, "EMAIL_ALREADY_EXISTS", "Email already registered");
        }

        // Password is stored plaintext for demo purposes (lab project).
        User user = new User(
                nextUserId(email),
                email.trim().toLowerCase(),
                fullName.trim(),
                phone == null ? "" : phone.trim(),
                password,
                "guest"
        );
        db.saveUser(user);
        return user;
    }

    private Long nextUserId(String email) {
        // We reuse db's "seq" by creating and reading a dummy user id.
        // This is intentionally simple for the lab.
        User tmp = db.nextUserId(email);
        return tmp.getId();
    }

    public TokenDetails login(String email, String password) {
        if (email == null || password == null) {
            throw new HotelException(400, "INVALID_CREDENTIALS", "Email and password are required");
        }

        User user = db.findUserByEmail(email)
                .orElseThrow(() -> new HotelException(401, "INVALID_CREDENTIALS", "Invalid email or password"));

        // Plaintext match for the lab.
        if (!user.getPassword().equals(password)) {
            throw new HotelException(401, "INVALID_CREDENTIALS", "Invalid email or password");
        }

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(TOKEN_EXPIRES_IN_SECONDS);
        tokenByValue.put(token, new TokenRecord(user.getId(), expiresAt));
        return new TokenDetails(token, TOKEN_EXPIRES_IN_SECONDS);
    }

    public List<RoomType> listRoomTypes(boolean activeOnly) {
        return db.listRoomTypes(activeOnly);
    }

    public List<Room> searchRooms(LocalDate checkIn, LocalDate checkOut, Long roomTypeId, Integer minCapacity) {
        if (checkIn == null || checkOut == null) {
            throw new HotelException(400, "INVALID_DATES", "checkIn and checkOut are required");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new HotelException(400, "INVALID_DATES", "checkIn must be before checkOut");
        }

        List<Room> allRooms = db.listRooms();
        return allRooms.stream()
                .filter(r -> "available".equalsIgnoreCase(r.getStatus()) || "cleaning".equalsIgnoreCase(r.getStatus()))
                .filter(r -> roomTypeId == null || r.getRoomTypeId().equals(roomTypeId))
                .filter(r -> {
                    if (minCapacity == null) return true;
                    RoomType rt = db.findRoomTypeById(r.getRoomTypeId()).orElse(null);
                    return rt != null && rt.getMaxCapacity() >= minCapacity;
                })
                .filter(r -> isRoomFreeDuringDates(r.getId(), checkIn, checkOut))
                .map(r -> r) // keep original fields
                .toList();
    }

    private boolean isRoomFreeDuringDates(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> bookings = db.findBookingsByRoomId(roomId);
        for (Booking b : bookings) {
            if ("pending".equalsIgnoreCase(b.getStatus()) || "active".equalsIgnoreCase(b.getStatus())) {
                if (datesOverlap(checkIn, checkOut, b.getCheckIn(), b.getCheckOut())) {
                    return false;
                }
            }
        }
        return true;
    }

    // Treat checkout date as exclusive boundary: [start, end)
    private static boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public Booking createBooking(String token, Long roomId, LocalDate checkIn, LocalDate checkOut, int guestsCount, String guestNotes) {
        Long userId = authenticate(token);

        if (guestsCount <= 0) {
            throw new HotelException(400, "INVALID_GUESTS_COUNT", "guestsCount must be positive");
        }
        if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
            throw new HotelException(400, "INVALID_DATES", "Invalid date range");
        }

        Room room = db.findRoomById(roomId)
                .orElseThrow(() -> new HotelException(404, "ROOM_NOT_FOUND", "Room not found"));

        if (!"available".equalsIgnoreCase(room.getStatus())) {
            throw new HotelException(409, "ROOM_NOT_AVAILABLE", "Room is not available");
        }

        if (!isRoomFreeDuringDates(room.getId(), checkIn, checkOut)) {
            throw new HotelException(409, "ROOM_NOT_AVAILABLE", "Room is already booked for selected dates");
        }

        Long bookingId = db.nextBookingId();
        Booking booking = new Booking(
                bookingId,
                roomId,
                checkIn,
                checkOut,
                guestsCount,
                guestNotes == null ? "" : guestNotes,
                "pending"
        );
        db.saveBooking(booking);
        // userId is tracked implicitly in this simplified lab (not stored in domain Booking).
        return booking;
    }

    public Room getRoomOrThrow(Long roomId) {
        return db.findRoomById(roomId)
                .orElseThrow(() -> new HotelException(404, "ROOM_NOT_FOUND", "Room not found"));
    }

    public Booking checkIn(String token, Long bookingId) {
        authenticate(token);

        Booking booking = db.findBookingById(bookingId)
                .orElseThrow(() -> new HotelException(404, "BOOKING_NOT_FOUND", "Booking not found"));

        if (!"pending".equalsIgnoreCase(booking.getStatus())) {
            throw new HotelException(409, "CHECK_IN_NOT_ALLOWED", "Check-in is not allowed for this booking");
        }

        db.updateRoomStatus(booking.getRoomId(), "occupied");
        booking.setStatus("active");
        return booking;
    }

    public Booking checkOut(String token, Long bookingId) {
        authenticate(token);

        Booking booking = db.findBookingById(bookingId)
                .orElseThrow(() -> new HotelException(404, "BOOKING_NOT_FOUND", "Booking not found"));

        if (!"active".equalsIgnoreCase(booking.getStatus())) {
            throw new HotelException(409, "CHECK_OUT_NOT_ALLOWED", "Check-out is not allowed for this booking");
        }

        db.updateRoomStatus(booking.getRoomId(), "available");
        booking.setStatus("completed");
        return booking;
    }

    public List<ServiceItem> listServices(boolean activeOnly) {
        return db.listServices(activeOnly);
    }

    public ServiceRequest createServiceRequest(String token, Long bookingId, Long serviceId, int quantity) {
        authenticate(token);

        if (quantity <= 0) {
            throw new HotelException(400, "INVALID_QUANTITY", "quantity must be positive");
        }

        Booking booking = db.findBookingById(bookingId)
                .orElseThrow(() -> new HotelException(404, "BOOKING_NOT_FOUND", "Booking not found"));

        if (!"active".equalsIgnoreCase(booking.getStatus())) {
            throw new HotelException(409, "SERVICE_REQUEST_NOT_ALLOWED", "Service can be requested only for active stay");
        }

        ServiceItem service = db.findServiceById(serviceId)
                .orElseThrow(() -> new HotelException(404, "SERVICE_NOT_FOUND", "Service not found"));

        if (!service.isActive()) {
            throw new HotelException(409, "SERVICE_NOT_AVAILABLE", "Service is not available");
        }

        Long requestId = db.nextServiceRequestId();
        ServiceRequest request = new ServiceRequest(
                requestId,
                bookingId,
                serviceId,
                quantity,
                "new",
                Instant.now()
        );
        db.saveServiceRequest(request);
        return request;
    }

    private Long authenticate(String authHeaderValue) {
        if (authHeaderValue == null || authHeaderValue.isBlank()) {
            throw new HotelException(401, "UNAUTHORIZED", "Missing Authorization header");
        }

        // Expected: "Bearer <token>"
        String token = authHeaderValue;
        if (authHeaderValue.toLowerCase().startsWith("bearer ")) {
            token = authHeaderValue.substring("bearer ".length()).trim();
        }

        TokenRecord rec = tokenByValue.get(token);
        if (rec == null) {
            throw new HotelException(401, "UNAUTHORIZED", "Invalid or expired token");
        }
        if (Instant.now().isAfter(rec.expiresAt)) {
            tokenByValue.remove(token);
            throw new HotelException(401, "UNAUTHORIZED", "Invalid or expired token");
        }
        return rec.userId;
    }
}

