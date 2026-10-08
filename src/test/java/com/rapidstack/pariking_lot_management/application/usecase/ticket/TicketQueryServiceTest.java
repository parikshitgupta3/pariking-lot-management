package com.rapidstack.pariking_lot_management.application.usecase.ticket;

import com.rapidstack.pariking_lot_management.application.port.ParkingLotRepository;
import com.rapidstack.pariking_lot_management.application.port.ParkingTicketRepository;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.model.ParkingFloor;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingSpot;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.domain.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TicketQueryServiceTest {

    private final ParkingTicketRepository tickets = mock(ParkingTicketRepository.class);
    private final ParkingLotRepository parkingLots = mock(ParkingLotRepository.class);

    private final TicketQueryService service = new TicketQueryService(tickets, parkingLots);

    private ParkingLot lot(String id, String name, ParkingSpot... spotsOnFloorOne) {
        return new ParkingLot(id, name, List.of(new ParkingFloor(id + "-f1", 1, List.of(spotsOnFloorOne))));
    }

    @Test
    void joinsFloorAndLotIntoEachActiveTicket() {
        ParkingSpot spotA = new ParkingSpot("s1", "A-1", SpotType.COMPACT);
        ParkingSpot spotB = new ParkingSpot("s2", "B-2", SpotType.LARGE);
        ParkingLot rapid = lot("lot-1", "Rapid Lot", spotA);
        ParkingLot harbour = lot("lot-2", "Harbour Lot", spotB);
        when(parkingLots.findAll()).thenReturn(List.of(rapid, harbour));

        ParkingTicket older = new ParkingTicket("t-old",
                new Vehicle("PQ12RS3456", VehicleType.TRUCK), spotB,
                Instant.parse("2026-10-04T08:00:00Z"));
        ParkingTicket newer = new ParkingTicket("t-new",
                new Vehicle("AB12CD3456", VehicleType.CAR), spotA,
                Instant.parse("2026-10-04T10:00:00Z"));
        when(tickets.findAllActive()).thenReturn(List.of(newer, older));

        List<ActiveTicketView> views = service.getActiveTickets();

        assertEquals(2, views.size());
        ActiveTicketView first = views.get(0);
        assertEquals("t-old", first.id());
        assertEquals("B-2", first.spotNumber());
        assertEquals(SpotType.LARGE, first.spotType());
        assertEquals(1, first.floorNumber());
        assertEquals("lot-2", first.lotId());
        assertEquals("Harbour Lot", first.lotName());

        ActiveTicketView second = views.get(1);
        assertEquals("t-new", second.id());
        assertEquals("A-1", second.spotNumber());
        assertEquals("lot-1", second.lotId());
        assertEquals("Rapid Lot", second.lotName());
    }

    @Test
    void rejectsTicketsWhoseSpotBelongsToNoKnownLot() {
        ParkingSpot floatingSpot = new ParkingSpot("s9", "Z-9", SpotType.COMPACT);
        when(parkingLots.findAll()).thenReturn(List.of());
        when(tickets.findAllActive()).thenReturn(List.of(new ParkingTicket("t1",
                new Vehicle("AB12CD3456", VehicleType.CAR), floatingSpot,
                Instant.parse("2026-10-04T10:00:00Z"))));

        assertThrows(NullPointerException.class, () -> service.getActiveTickets());
    }

    @Test
    void returnsEmptyListWhenNoTicketsAreActive() {
        when(parkingLots.findAll()).thenReturn(List.of());
        when(tickets.findAllActive()).thenReturn(List.of());

        assertEquals(List.of(), service.getActiveTickets());
    }
}
