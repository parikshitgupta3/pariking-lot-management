package com.rapidstack.pariking_lot_management.domain.model;

import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;
import com.rapidstack.pariking_lot_management.domain.enums.TicketStatus;
import com.rapidstack.pariking_lot_management.domain.enums.VehicleType;
import com.rapidstack.pariking_lot_management.domain.exception.TicketAlreadyCompletedException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkingTicketTest {

    private static final Instant ENTRY = Instant.parse("2026-10-04T10:00:00Z");
    private static final Instant EXIT = Instant.parse("2026-10-04T11:30:00Z");
    private static final BigDecimal FEE = new BigDecimal("15.00");

    private ParkingTicket activeTicket() {
        ParkingSpot spot = new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED);
        return new ParkingTicket("t1", new Vehicle("AB12CD3456", VehicleType.CAR), spot, ENTRY);
    }

    @Test
    void activeTicketHasNoExitTimeAndNoFee() {
        ParkingTicket ticket = activeTicket();

        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertNull(ticket.getExitTime());
        assertNull(ticket.getFee());
    }

    @Test
    void closeRecordsExitTimeFeeAndCompletesTheTicket() {
        ParkingTicket ticket = activeTicket();

        ticket.close(EXIT, FEE);

        assertEquals(TicketStatus.COMPLETED, ticket.getStatus());
        assertEquals(EXIT, ticket.getExitTime());
        assertEquals(0, FEE.compareTo(ticket.getFee()));
    }

    @Test
    void closeRejectsExitTimeBeforeEntryTime() {
        ParkingTicket ticket = activeTicket();

        assertThrows(IllegalArgumentException.class,
                () -> ticket.close(ENTRY.minusSeconds(1), FEE));
    }

    @Test
    void closeRejectsNegativeFee() {
        ParkingTicket ticket = activeTicket();

        assertThrows(IllegalArgumentException.class,
                () -> ticket.close(EXIT, new BigDecimal("-0.01")));
    }

    @Test
    void closeRejectsNullArguments() {
        ParkingTicket ticket = activeTicket();

        assertThrows(NullPointerException.class, () -> ticket.close(null, FEE));
        assertThrows(NullPointerException.class, () -> ticket.close(EXIT, null));
    }

    @Test
    void ticketCannotBeClosedTwice() {
        ParkingTicket ticket = activeTicket();
        ticket.close(EXIT, FEE);

        assertThrows(TicketAlreadyCompletedException.class, () -> ticket.close(EXIT, FEE));
    }

    @Test
    void completedTicketRequiresExitTimeAndFee() {
        ParkingSpot spot = new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED);
        Vehicle car = new Vehicle("AB12CD3456", VehicleType.CAR);

        assertThrows(NullPointerException.class,
                () -> new ParkingTicket("t1", car, spot, ENTRY, null, FEE, TicketStatus.COMPLETED));
        assertThrows(NullPointerException.class,
                () -> new ParkingTicket("t1", car, spot, ENTRY, EXIT, null, TicketStatus.COMPLETED));
        assertThrows(IllegalArgumentException.class,
                () -> new ParkingTicket("t1", car, spot, ENTRY, ENTRY.minusSeconds(1), FEE,
                        TicketStatus.COMPLETED));
    }

    @Test
    void activeTicketMustNotHaveExitTimeOrFee() {
        ParkingSpot spot = new ParkingSpot("s1", "A-1", SpotType.COMPACT, SpotStatus.OCCUPIED);
        Vehicle car = new Vehicle("AB12CD3456", VehicleType.CAR);

        assertThrows(IllegalArgumentException.class,
                () -> new ParkingTicket("t1", car, spot, ENTRY, EXIT, null, TicketStatus.ACTIVE));
        assertThrows(IllegalArgumentException.class,
                () -> new ParkingTicket("t1", car, spot, ENTRY, null, FEE, TicketStatus.ACTIVE));
    }
}
