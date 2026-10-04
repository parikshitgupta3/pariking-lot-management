package com.rapidstack.pariking_lot_management.api;

import com.rapidstack.pariking_lot_management.application.exception.TicketNotFoundException;
import com.rapidstack.pariking_lot_management.application.usecase.exit.ParkingExitService;
import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.TicketStatus;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.exception.TicketAlreadyCompletedException;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    private static final Instant ENTRY = Instant.parse("2026-10-04T10:00:00Z");
    private static final Instant EXIT = Instant.parse("2026-10-04T11:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParkingExitService parkingExitService;

    private ParkingTicket activeTicket() {
        ParkingSpot spot = new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED);
        return new ParkingTicket("t1", new Vehicle("AB12CD3456", VehicleType.CAR), spot, ENTRY);
    }

    private ParkingTicket completedTicket() {
        ParkingSpot spot = new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED);
        return new ParkingTicket("t1", new Vehicle("AB12CD3456", VehicleType.CAR), spot,
                ENTRY, EXIT, new BigDecimal("15.00"), TicketStatus.COMPLETED);
    }

    @Test
    void checkoutReturnsCompletedTicket() throws Exception {
        when(parkingExitService.checkout("t1")).thenReturn(completedTicket());

        mockMvc.perform(post("/api/v1/tickets/t1/exit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t1"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.exitTime").value("2026-10-04T11:30:00Z"))
                .andExpect(jsonPath("$.fee").value(15.00))
                .andExpect(jsonPath("$.vehicleRegistrationNumber").value("AB12CD3456"))
                .andExpect(jsonPath("$.spotNumber").value("A-1"));
    }

    @Test
    void checkoutReturns409ForAlreadyCompletedTicket() throws Exception {
        when(parkingExitService.checkout("t1"))
                .thenThrow(new TicketAlreadyCompletedException("Ticket t1 is already completed"));

        mockMvc.perform(post("/api/v1/tickets/t1/exit"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Ticket t1 is already completed"));
    }

    @Test
    void checkoutReturns404ForUnknownTicket() throws Exception {
        when(parkingExitService.checkout("missing"))
                .thenThrow(new TicketNotFoundException("No parking ticket found for id 'missing'"));

        mockMvc.perform(post("/api/v1/tickets/missing/exit"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getReturnsActiveTicketDetails() throws Exception {
        when(parkingExitService.getTicket("t1")).thenReturn(activeTicket());

        mockMvc.perform(get("/api/v1/tickets/t1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.entryTime").value("2026-10-04T10:00:00Z"))
                .andExpect(jsonPath("$.exitTime").doesNotExist())
                .andExpect(jsonPath("$.fee").doesNotExist());
    }

    @Test
    void getReturns404ForUnknownTicket() throws Exception {
        when(parkingExitService.getTicket("missing"))
                .thenThrow(new TicketNotFoundException("No parking ticket found for id 'missing'"));

        mockMvc.perform(get("/api/v1/tickets/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No parking ticket found for id 'missing'"));
    }
}
