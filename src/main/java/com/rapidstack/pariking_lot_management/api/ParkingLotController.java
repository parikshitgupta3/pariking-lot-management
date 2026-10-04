package com.rapidstack.pariking_lot_management.api;

import com.rapidstack.pariking_lot_management.api.dto.AvailabilityResponse;
import com.rapidstack.pariking_lot_management.api.dto.CreateParkingLotRequest;
import com.rapidstack.pariking_lot_management.api.dto.ParkingLotDetailsResponse;
import com.rapidstack.pariking_lot_management.api.dto.ParkingLotSummaryResponse;
import com.rapidstack.pariking_lot_management.api.dto.TicketResponse;
import com.rapidstack.pariking_lot_management.api.dto.VehicleEntryRequest;
import com.rapidstack.pariking_lot_management.application.usecase.entry.ParkingEntryService;
import com.rapidstack.pariking_lot_management.application.usecase.parkinglot.ParkingLotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * REST endpoints for parking-lot management, vehicle entry, and
 * availability queries. Controllers only translate HTTP to use-case calls
 * and back — all behaviour lives in the application services and domain.
 */
@RestController
@RequestMapping("/api/v1/parking-lots")
@Tag(name = "Parking Lots", description = "Manage parking lots, admit vehicles, and query availability")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;
    private final ParkingEntryService parkingEntryService;

    public ParkingLotController(ParkingLotService parkingLotService, ParkingEntryService parkingEntryService) {
        this.parkingLotService = parkingLotService;
        this.parkingEntryService = parkingEntryService;
    }

    @PostMapping
    @Operation(summary = "Create a parking lot with its floors and spots")
    public ResponseEntity<ParkingLotDetailsResponse> create(@Valid @RequestBody CreateParkingLotRequest request) {
        var lot = parkingLotService.create(request.toDomain());
        return ResponseEntity
                .created(URI.create("/api/v1/parking-lots/" + lot.getId()))
                .body(ParkingLotDetailsResponse.from(lot));
    }

    @GetMapping
    @Operation(summary = "List all parking lots")
    public List<ParkingLotSummaryResponse> list() {
        return parkingLotService.getAll().stream()
                .map(ParkingLotSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a parking lot's details, including every spot's status")
    public ParkingLotDetailsResponse get(@PathVariable String id) {
        return ParkingLotDetailsResponse.from(parkingLotService.getById(id));
    }

    @PostMapping("/{id}/vehicles/entry")
    @Operation(summary = "Admit a vehicle to a lot and open a ticket")
    public ResponseEntity<TicketResponse> enterVehicle(@PathVariable String id,
                                                       @Valid @RequestBody VehicleEntryRequest request) {
        var ticket = parkingEntryService.admit(request.toDomain(), parkingLotService.getById(id));
        return ResponseEntity
                .created(URI.create("/api/v1/tickets/" + ticket.getId()))
                .body(TicketResponse.from(ticket));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get the lot's currently available spots")
    public AvailabilityResponse availability(@PathVariable String id) {
        return AvailabilityResponse.from(parkingLotService.getAvailability(id));
    }
}
