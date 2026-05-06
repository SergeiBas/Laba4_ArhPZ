package com.laba4.hotel.presentation;

import com.laba4.hotel.application.HotelApplicationService;
import com.laba4.hotel.domain.Booking;
import com.laba4.hotel.domain.Room;
import com.laba4.hotel.domain.RoomType;
import com.laba4.hotel.domain.ServiceItem;
import com.laba4.hotel.domain.ServiceRequest;
import com.laba4.hotel.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class HotelController {

    private final HotelApplicationService service;

    public HotelController(HotelApplicationService service) {
        this.service = service;
    }

    // -------------------- Auth --------------------

    public static record UserCreatedResponse(Long id, String email, String role) {}

    public static record TokenResponse(String accessToken, String tokenType, long expiresIn) {}

    public static record GuestRegisterRequest(
            @NotBlank String email,
            @NotBlank String password,
            @NotBlank String fullName,
            String phone
    ) {}

    public static record GuestLoginRequest(
            @NotBlank String email,
            @NotBlank String password
    ) {}

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserCreatedResponse register(@Valid @RequestBody GuestRegisterRequest request) {
        User u = service.register(request.email(), request.password(), request.fullName(), request.phone());
        return new UserCreatedResponse(u.getId(), u.getEmail(), u.getRole());
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@Valid @RequestBody GuestLoginRequest request) {
        HotelApplicationService.TokenDetails details = service.login(request.email(), request.password());
        return new TokenResponse(details.accessToken(), "Bearer", details.expiresIn());
    }

    // -------------------- Room types & availability --------------------

    public static record RoomTypeResponse(
            Long id,
            String name,
            String description,
            int baseCapacity,
            int maxCapacity,
            boolean active
    ) {}

    @GetMapping("/room-types")
    public List<RoomTypeResponse> listRoomTypes(@RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        return service.listRoomTypes(activeOnly).stream()
                .map(rt -> new RoomTypeResponse(
                        rt.getId(),
                        rt.getName(),
                        rt.getDescription(),
                        rt.getBaseCapacity(),
                        rt.getMaxCapacity(),
                        rt.isActive()
                ))
                .toList();
    }

    public static record RoomResponse(
            Long id,
            String number,
            int floor,
            Long roomTypeId,
            String roomTypeName,
            String status,
            double nightlyRate,
            String currency
    ) {}

    @GetMapping("/rooms")
    public List<RoomResponse> listRooms(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Integer minCapacity
    ) {
        // Build lookup for room type names (seeded in in-memory DB).
        Map<Long, String> roomTypeNames = service.listRoomTypes(true).stream()
                .collect(Collectors.toMap(RoomType::getId, RoomType::getName));

        return service.searchRooms(checkIn, checkOut, roomTypeId, minCapacity).stream()
                .map(r -> new RoomResponse(
                        r.getId(),
                        r.getNumber(),
                        r.getFloor(),
                        r.getRoomTypeId(),
                        roomTypeNames.getOrDefault(r.getRoomTypeId(), ""),
                        r.getStatus(),
                        r.getNightlyRate(),
                        r.getCurrency()
                ))
                .toList();
    }

    // -------------------- Bookings --------------------

    public static record CreateBookingRequest(
            @NotNull Long roomId,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @Min(1) int guestsCount,
            String guestNotes
    ) {}

    public static record BookingResponse(
            Long id,
            Long roomId,
            String roomStatus,
            LocalDate checkIn,
            LocalDate checkOut,
            int guestsCount,
            String guestNotes,
            String status
    ) {}

    private String requireBearerToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || authorization.isBlank()) {
            // Application layer uses HotelException and will return 401.
            return authorization;
        }
        return authorization;
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        Booking b = service.createBooking(authorization, request.roomId(), request.checkIn(), request.checkOut(), request.guestsCount(), request.guestNotes());
        Room room = service.getRoomOrThrow(b.getRoomId());
        return toBookingResponse(b, room.getStatus());
    }

    @PostMapping("/bookings/{bookingId}/check-in")
    public BookingResponse checkIn(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long bookingId,
            @RequestBody(required = false) Map<String, Object> ignored
    ) {
        Booking b = service.checkIn(authorization, bookingId);
        Room room = service.getRoomOrThrow(b.getRoomId());
        return toBookingResponse(b, room.getStatus());
    }

    @PostMapping("/bookings/{bookingId}/check-out")
    public BookingResponse checkOut(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long bookingId
    ) {
        Booking b = service.checkOut(authorization, bookingId);
        Room room = service.getRoomOrThrow(b.getRoomId());
        return toBookingResponse(b, room.getStatus());
    }

    private BookingResponse toBookingResponse(Booking b, String roomStatus) {
        return new BookingResponse(
                b.getId(),
                b.getRoomId(),
                roomStatus,
                b.getCheckIn(),
                b.getCheckOut(),
                b.getGuestsCount(),
                b.getGuestNotes(),
                b.getStatus()
        );
    }

    // -------------------- Services --------------------

    public static record ServiceResponse(
            Long id,
            String name,
            String description,
            boolean active,
            double pricePerUnit,
            String currency
    ) {}

    @GetMapping("/services")
    public List<ServiceResponse> listServices(@RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        return service.listServices(activeOnly).stream()
                .map(s -> new ServiceResponse(s.getId(), s.getName(), s.getDescription(), s.isActive(), s.getPricePerUnit(), s.getCurrency()))
                .toList();
    }

    public static record CreateServiceRequest(
            @NotNull Long bookingId,
            @NotNull Long serviceId,
            @Min(1) int quantity
    ) {}

    public static record ServiceRequestResponse(
            Long id,
            Long bookingId,
            Long serviceId,
            int quantity,
            String status
    ) {}

    @PostMapping("/service-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceRequestResponse createServiceRequest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateServiceRequest request
    ) {
        ServiceRequest sr = service.createServiceRequest(authorization, request.bookingId(), request.serviceId(), request.quantity());
        return new ServiceRequestResponse(sr.getId(), sr.getBookingId(), sr.getServiceId(), sr.getQuantity(), sr.getStatus());
    }
}

