package com.laba4.hotel.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void hotelFlow_register_login_search_book_checkin_serviceRequest() throws Exception {
        String registerJson = """
                {
                  "email": "guest@mail.com",
                  "password": "SecurePass1",
                  "fullName": "Іваненко Іван Іванович",
                  "phone": "+380501234567"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("guest@mail.com"))
                .andExpect(jsonPath("$.role").value("guest"));

        String loginJson = """
                {
                  "email": "guest@mail.com",
                  "password": "SecurePass1"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.get("accessToken").asText();

        mockMvc.perform(get("/api/v1/room-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());

        String checkIn = "2026-06-01";
        String checkOut = "2026-06-03";

        mockMvc.perform(get("/api/v1/rooms")
                        .param("checkIn", checkIn)
                        .param("checkOut", checkOut))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());

        // Use a seeded room id 101.
        String createBookingJson = """
                {
                  "roomId": 101,
                  "checkIn": "%s",
                  "checkOut": "%s",
                  "guestsCount": 2,
                  "guestNotes": "Late check-in"
                }
                """.formatted(checkIn, checkOut);

        MvcResult createBookingResult = mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBookingJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.roomId").value(101))
                .andExpect(jsonPath("$.status").value("pending"))
                .andReturn();

        JsonNode bookingBody = objectMapper.readTree(createBookingResult.getResponse().getContentAsString());
        long bookingId = bookingBody.get("id").asLong();

        mockMvc.perform(post("/api/v1/bookings/" + bookingId + "/check-in")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.roomStatus").value("occupied"));

        // Create an additional service request for the active stay.
        String serviceRequestJson = """
                {
                  "bookingId": %d,
                  "serviceId": 1,
                  "quantity": 2
                }
                """.formatted(bookingId);

        mockMvc.perform(post("/api/v1/service-requests")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serviceRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.bookingId").value(bookingId))
                .andExpect(jsonPath("$.serviceId").value(1))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("new"));
    }
}

