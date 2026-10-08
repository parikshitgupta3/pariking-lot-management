package com.rapidstack.pariking_lot_management.infrastructure.persistence.adapter;

import com.rapidstack.pariking_lot_management.application.port.ParkingTicketRepository;
import com.rapidstack.pariking_lot_management.domain.model.ParkingTicket;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingSpotEntity;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.VehicleEntity;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper.ParkingTicketMapper;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper.VehicleMapper;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.repository.ParkingSpotJpaRepository;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.repository.ParkingTicketJpaRepository;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.repository.VehicleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * The JPA-backed implementation of the application's
 * {@link ParkingTicketRepository} port.
 *
 * <p>Saving a ticket upserts the vehicle (natural key = registration
 * number), updates the referenced spot's status to match the domain object,
 * and then writes the ticket row — all in one transaction. The spot must
 * already be persisted (it belongs to a stored lot aggregate); a ticket for
 * an unknown spot is rejected loudly.
 */
@Repository
public class ParkingTicketRepositoryAdapter implements ParkingTicketRepository {

    private final ParkingTicketJpaRepository tickets;
    private final VehicleJpaRepository vehicles;
    private final ParkingSpotJpaRepository spots;

    public ParkingTicketRepositoryAdapter(ParkingTicketJpaRepository tickets,
                                          VehicleJpaRepository vehicles,
                                          ParkingSpotJpaRepository spots) {
        this.tickets = tickets;
        this.vehicles = vehicles;
        this.spots = spots;
    }

    @Override
    public Optional<ParkingTicket> findById(String ticketId) {
        return tickets.findById(ticketId)
                .map(ParkingTicketMapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<ParkingTicket> findByIdForUpdate(String ticketId) {
        return tickets.findByIdForUpdate(ticketId)
                .map(ParkingTicketMapper::toDomain);
    }

    @Override
    @Transactional
    public void save(ParkingTicket ticket) {
        VehicleEntity vehicle = vehicles.save(VehicleMapper.toEntity(ticket.getVehicle()));
        ParkingSpotEntity spot = spots.findById(ticket.getSpot().getId())
                .orElseThrow(() -> new SpotNotPersistedException(
                        "Cannot save ticket " + ticket.getId() + ": spot " + ticket.getSpot().getId()
                                + " is not persisted. Persist the owning lot first."));
        spot.setStatus(ticket.getSpot().getStatus());
        spots.save(spot);
        tickets.save(ParkingTicketMapper.toEntity(ticket, vehicle, spot));
    }
}
