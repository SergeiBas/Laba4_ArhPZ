package com.laba4.hotel.data;

import com.laba4.hotel.domain.Booking;
import com.laba4.hotel.domain.Room;
import com.laba4.hotel.domain.RoomType;
import com.laba4.hotel.domain.ServiceItem;
import com.laba4.hotel.domain.ServiceRequest;
import com.laba4.hotel.domain.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryHotelDatabase {
    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();
    private final Map<Long, User> usersById = new ConcurrentHashMap<>();

    private final Map<Long, RoomType> roomTypes = new ConcurrentHashMap<>();
    private final Map<Long, Room> rooms = new ConcurrentHashMap<>();
    private final Map<Long, Booking> bookings = new ConcurrentHashMap<>();
    private final Map<Long, List<Booking>> bookingsByRoomId = new ConcurrentHashMap<>();

    private final Map<Long, ServiceItem> services = new ConcurrentHashMap<>();
    private final Map<Long, ServiceRequest> serviceRequests = new ConcurrentHashMap<>();

    private final AtomicLong userSeq = new AtomicLong(1);
    private final AtomicLong roomTypeSeq = new AtomicLong(1);
    private final AtomicLong bookingSeq = new AtomicLong(1000);
    private final AtomicLong serviceRequestSeq = new AtomicLong(2000);

    public InMemoryHotelDatabase() {
        seedRoomTypes();
        seedRooms();
        seedServices();
    }

    // -------------------- Users --------------------

    public Optional<User> findUserByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(normalizeEmail(email)));
    }

    public User saveUser(User user) {
        usersByEmail.put(normalizeEmail(user.getEmail()), user);
        usersById.put(user.getId(), user);
        return user;
    }

    public User nextUserId(String email) {
        return new User(userSeq.getAndIncrement(), normalizeEmail(email), "", "", "", "guest");
    }

    // -------------------- Room Types --------------------

    public List<RoomType> listRoomTypes(boolean activeOnly) {
        List<RoomType> all = new ArrayList<>(roomTypes.values());
        return all.stream()
                .filter(rt -> !activeOnly || rt.isActive())
                .sorted(Comparator.comparing(RoomType::getId))
                .toList();
    }

    public Optional<RoomType> findRoomTypeById(Long roomTypeId) {
        return Optional.ofNullable(roomTypes.get(roomTypeId));
    }

    // -------------------- Rooms --------------------

    public Optional<Room> findRoomById(Long roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    public List<Room> listRooms() {
        return rooms.values().stream()
                .sorted(Comparator.comparing(Room::getId))
                .toList();
    }

    public Room updateRoomStatus(Long roomId, String status) {
        Room room = rooms.get(roomId);
        if (room == null) return null;
        room.setStatus(status);
        return room;
    }

    public RoomType createRoomType(String name, String description, int baseCapacity, int maxCapacity, boolean active) {
        long id = roomTypeSeq.getAndIncrement();
        RoomType roomType = new RoomType(id, name, description, baseCapacity, maxCapacity, active);
        roomTypes.put(id, roomType);
        return roomType;
    }

    // -------------------- Bookings --------------------

    public Optional<Booking> findBookingById(Long bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    public Booking saveBooking(Booking booking) {
        bookings.put(booking.getId(), booking);
        bookingsByRoomId.computeIfAbsent(booking.getRoomId(), k -> new ArrayList<>()).add(booking);
        return booking;
    }

    public List<Booking> findBookingsByRoomId(Long roomId) {
        return bookingsByRoomId.getOrDefault(roomId, List.of())
                .stream()
                .sorted(Comparator.comparing(Booking::getId))
                .toList();
    }

    public Booking updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) return null;
        booking.setStatus(status);
        return booking;
    }

    // -------------------- Services --------------------

    public List<ServiceItem> listServices(boolean activeOnly) {
        return services.values().stream()
                .filter(s -> !activeOnly || s.isActive())
                .sorted(Comparator.comparing(ServiceItem::getId))
                .toList();
    }

    public Optional<ServiceItem> findServiceById(Long serviceId) {
        return Optional.ofNullable(services.get(serviceId));
    }

    public ServiceRequest saveServiceRequest(ServiceRequest request) {
        serviceRequests.put(request.getId(), request);
        return request;
    }

    public Long nextBookingId() {
        return bookingSeq.getAndIncrement();
    }

    public Long nextServiceRequestId() {
        return serviceRequestSeq.getAndIncrement();
    }

    // -------------------- Seeding --------------------

    private void seedRoomTypes() {
        // Hotel sample data based on the lab variant.
        RoomType standard = new RoomType(1L, "Стандарт", "Однокімнатний номер", 2, 3, true);
        RoomType suite = new RoomType(2L, "Люкс", "Два кімнати, ванна з джакузі", 2, 4, true);
        roomTypes.put(standard.getId(), standard);
        roomTypes.put(suite.getId(), suite);
        roomTypeSeq.set(3L);
    }

    private void seedRooms() {
        rooms.put(101L, new Room(101L, "101", 1, 1L, "available", 2500.00, "UAH"));
        rooms.put(102L, new Room(102L, "102", 1, 1L, "available", 2500.00, "UAH"));
        rooms.put(201L, new Room(201L, "201", 2, 2L, "available", 4500.00, "UAH"));
    }

    private void seedServices() {
        services.put(1L, new ServiceItem(1L, "Room service", "Замовлення їжі та напоїв", true, 500.00, "UAH"));
        services.put(2L, new ServiceItem(2L, "Прибирання", "Додаткове прибирання номеру", true, 300.00, "UAH"));
        services.put(3L, new ServiceItem(3L, "Трансфер", "Трансфер до/з аеропорту", true, 1200.00, "UAH"));
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}

