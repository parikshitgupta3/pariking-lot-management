package com.rapidstack.pariking_lot_management.api;

import com.rapidstack.pariking_lot_management.application.exception.ParkingLotNotFoundException;
import com.rapidstack.pariking_lot_management.application.usecase.entry.ParkingEntryService;
import com.rapidstack.pariking_lot_management.application.usecase.parkinglot.AvailableSpotView;
import com.rapidstack.pariking_lot_management.application.usecase.parkinglot.ParkingLotService;
import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.TicketStatus;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.exception.NoAvailableSpotException;
import com.rapidstack.pariking_lot_management.domain.model.ParkingFloor;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingLotController.class)
class ParkingLotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParkingLotService parkingLotService;

    @MockitoBean
    private ParkingEntryService parkingEntryService;

    private static final String CREATE_LOT_BODY = """
            {
              "name": "Rapid Lot",
              "floors": [
                {
                  "floorNumber": 1,
                  "spots": [
                    {"spotNumber": "A-1", "spotType": "COMPACT"},
                    {"spotNumber": "A-2", "spotType": "BIKE"}
                  ]
                }
              ]
            }
            """;

    private ParkingLot sampleLot() {
        return new ParkingLot("lot-1", "Rapid Lot", List.of(
                new ParkingFloor("f1", 1, List.of(
                        new ParkingSpot("s1", "A-1", SpotType.COMPACT),
                        new ParkingSpot("s2", "A-2", SpotType.BIKE)))));
    }

    @Test
    void createReturns201WithLocationAndPersistedDetails() throws Exception {
        when(parkingLotService.create(any(ParkingLot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_LOT_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/v1/parking-lots/.+")))
                .andExpect(jsonPath("$.name").value("Rapid Lot"))
                .andExpect(jsonPath("$.floors[0].floorNumber").value(1))
                .andExpect(jsonPath("$.floors[0].spots[0].spotNumber").value("A-1"))
                .andExpect(jsonPath("$.floors[0].spots[0].spotType").value("COMPACT"))
                .andExpect(jsonPath("$.floors[0].spots[0].status").value("AVAILABLE"));

        verify(parkingLotService).create(argThat(lot ->
                "Rapid Lot".equals(lot.getName())
                        && lot.getFloors().size() == 1
                        && lot.getFloors().get(0).getSpots().size() == 2
                        && "A-1".equals(lot.getFloors().get(0).getSpots().get(0).getSpotNumber())));
    }

    @Test
    void createRejectsInvalidBodiesWith400AndFieldErrors() throws Exception {
        postAndExpect400("""
                {"name": "", "floors": []}
                """);
        postAndExpect400("""
                {"name": "Rapid Lot", "floors": [{"floorNumber": 1, "spots": [{"spotNumber": "", "spotType": "COMPACT"}]}]}
                """);
    }

    private void postAndExpect400(String body) throws Exception {
        mockMvc.perform(post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());
    }

    @Test
    void createRejectsMalformedJsonWith400() throws Exception {
        mockMvc.perform(post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listReturnsSummaries() throws Exception {
        when(parkingLotService.getAll()).thenReturn(List.of(sampleLot()));

        mockMvc.perform(get("/api/v1/parking-lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("lot-1"))
                .andExpect(jsonPath("$[0].name").value("Rapid Lot"))
                .andExpect(jsonPath("$[0].floorCount").value(1))
                .andExpect(jsonPath("$[0].totalSpots").value(2));
    }

    @Test
    void getReturnsFullDetails() throws Exception {
        ParkingLot lot = sampleLot();
        ParkingSpot occupied = lot.getFloors().get(0).getSpots().get(0);
        occupied.markOccupied();
        ParkingTicket ticket = new ParkingTicket("t-active",
                new Vehicle("AB12CD3456", VehicleType.CAR), occupied,
                Instant.parse("2026-10-04T10:15:30Z"));
        when(parkingLotService.getDetail("lot-1"))
                .thenReturn(new ParkingLotService.LotDetail(lot, Map.of(occupied.getId(), ticket)));

        mockMvc.perform(get("/api/v1/parking-lots/lot-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("lot-1"))
                .andExpect(jsonPath("$.floors[0].spots[0].spotNumber").value("A-1"))
                .andExpect(jsonPath("$.floors[0].spots[0].status").value("OCCUPIED"))
                .andExpect(jsonPath("$.floors[0].spots[0].ticket.id").value("t-active"))
                .andExpect(jsonPath("$.floors[0].spots[0].ticket.vehicleRegistrationNumber").value("AB12CD3456"))
                .andExpect(jsonPath("$.floors[0].spots[0].ticket.vehicleType").value("CAR"))
                .andExpect(jsonPath("$.floors[0].spots[0].ticket.entryTime").value("2026-10-04T10:15:30Z"))
                .andExpect(jsonPath("$.floors[0].spots[1].spotNumber").value("A-2"))
                .andExpect(jsonPath("$.floors[0].spots[1].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.floors[0].spots[1].ticket").value(nullValue()));
    }

    @Test
    void getReturns404ForUnknownLot() throws Exception {
        when(parkingLotService.getDetail("missing"))
                .thenThrow(new ParkingLotNotFoundException("No parking lot found for id 'missing'"));

        mockMvc.perform(get("/api/v1/parking-lots/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No parking lot found for id 'missing'"));
    }

    @Test
    void vehicleEntryReturns201WithActiveTicket() throws Exception {
        ParkingLot lot = sampleLot();
        ParkingSpot spot = lot.getFloors().get(0).getSpots().get(0);
        ParkingTicket ticket = new ParkingTicket("t1",
                new Vehicle("AB12CD3456", VehicleType.CAR), spot, Instant.parse("2026-10-04T10:15:30Z"));
        when(parkingLotService.getById("lot-1")).thenReturn(lot);
        when(parkingEntryService.admit(any(Vehicle.class), eq(lot))).thenReturn(ticket);

        mockMvc.perform(post("/api/v1/parking-lots/lot-1/vehicles/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"registrationNumber": "ab12cd3456", "vehicleType": "CAR"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/v1/tickets/.+")))
                .andExpect(jsonPath("$.id").value("t1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.vehicleRegistrationNumber").value("AB12CD3456"))
                .andExpect(jsonPath("$.spotNumber").value("A-1"))
                .andExpect(jsonPath("$.entryTime").value("2026-10-04T10:15:30Z"));

        verify(parkingEntryService).admit(argThat(vehicle ->
                vehicle.getRegistrationNumber().equals("AB12CD3456") // canonicalised by the domain
                        && vehicle.getVehicleType() == VehicleType.CAR), eq(lot));
    }

    @Test
    void vehicleEntryReturns409WhenNoSpotFits() throws Exception {
        when(parkingLotService.getById("lot-1")).thenReturn(sampleLot());
        when(parkingEntryService.admit(any(Vehicle.class), any(ParkingLot.class)))
                .thenThrow(new NoAvailableSpotException("No available spot for vehicle AB12CD3456 (CAR)"));

        mockMvc.perform(post("/api/v1/parking-lots/lot-1/vehicles/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"registrationNumber": "AB12CD3456", "vehicleType": "CAR"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void vehicleEntryReturns404ForUnknownLot() throws Exception {
        when(parkingLotService.getById("missing"))
                .thenThrow(new ParkingLotNotFoundException("No parking lot found for id 'missing'"));

        mockMvc.perform(post("/api/v1/parking-lots/missing/vehicles/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"registrationNumber": "AB12CD3456", "vehicleType": "CAR"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void vehicleEntryRejectsBlankRegistrationNumberWith400AndFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/parking-lots/lot-1/vehicles/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"registrationNumber": "", "vehicleType": "CAR"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.registrationNumber").exists());
    }

    @Test
    void vehicleEntryRejectsUnknownVehicleTypesWith400() throws Exception {
        mockMvc.perform(post("/api/v1/parking-lots/lot-1/vehicles/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"registrationNumber": "AB12CD3456", "vehicleType": "SPACESHIP"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void availabilityReturnsFreeSpotsWithFloors() throws Exception {
        when(parkingLotService.getAvailability("lot-1")).thenReturn(List.of(
                new AvailableSpotView("s1", 1, "A-1", SpotType.COMPACT),
                new AvailableSpotView("s3", 2, "B-1", SpotType.LARGE)));

        mockMvc.perform(get("/api/v1/parking-lots/lot-1/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSpotCount").value(2))
                .andExpect(jsonPath("$.spots[0].floorNumber").value(1))
                .andExpect(jsonPath("$.spots[0].spotNumber").value("A-1"))
                .andExpect(jsonPath("$.spots[0].spotType").value("COMPACT"))
                .andExpect(jsonPath("$.spots[1].floorNumber").value(2))
                .andExpect(jsonPath("$.spots[1].spotType").value("LARGE"));
    }
}
