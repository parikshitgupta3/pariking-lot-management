package com.rapidstack.pariking_lot_management.api;

import com.rapidstack.pariking_lot_management.api.dto.ActiveTicketResponse;
import com.rapidstack.pariking_lot_management.api.dto.TicketResponse;
import com.rapidstack.pariking_lot_management.application.usecase.exit.ParkingExitService;
import com.rapidstack.pariking_lot_management.application.usecase.ticket.ActiveTicketView;
import com.rapidstack.pariking_lot_management.application.usecase.ticket.TicketQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for ticket lookup, active-ticket listing, and vehicle exit.
 * Controllers only translate HTTP to use-case calls and back — all behaviour
 * lives in the application services and domain.
 */
@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Look up tickets, list active tickets, and check out vehicles")
public class TicketController {

    private final ParkingExitService parkingExitService;
    private final TicketQueryService ticketQueryService;

    public TicketController(ParkingExitService parkingExitService, TicketQueryService ticketQueryService) {
        this.parkingExitService = parkingExitService;
        this.ticketQueryService = ticketQueryService;
    }

    @GetMapping("/active")
    @Operation(summary = "List every active ticket across all lots, oldest entry first")
    public List<ActiveTicketResponse> activeTickets() {
        return ticketQueryService.getActiveTickets().stream()
                .map(ActiveTicketResponse::from)
                .toList();
    }

    @PostMapping("/{id}/exit")
    @Operation(summary = "Check out the vehicle holding the ticket and return the completed ticket")
    public TicketResponse checkout(@PathVariable String id) {
        return TicketResponse.from(parkingExitService.checkout(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a ticket's details")
    public TicketResponse get(@PathVariable String id) {
        return TicketResponse.from(parkingExitService.getTicket(id));
    }
}
