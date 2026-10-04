package com.rapidstack.pariking_lot_management.application.usecase.parkinglot;

import com.rapidstack.pariking_lot_management.application.exception.ParkingLotNotFoundException;
import com.rapidstack.pariking_lot_management.application.port.ParkingLotRepository;
import com.rapidstack.pariking_lot_management.domain.enums.SpotStatus;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Orchestrates parking-lot management: creating lots (with their floors and
 * spots), looking them up, and answering availability queries. The service
 * owns no business rules — structural invariants (unique floor/spot numbers)
 * live in the domain model and the database; this class only delegates to
 * the {@link ParkingLotRepository} port and assembles query views.
 */
@Service
public class ParkingLotService {

    private final ParkingLotRepository parkingLots;

    public ParkingLotService(ParkingLotRepository parkingLots) {
        this.parkingLots = Objects.requireNonNull(parkingLots, "parkingLots must not be null");
    }

    public ParkingLot create(ParkingLot lot) {
        return parkingLots.save(lot);
    }

    public ParkingLot getById(String id) {
        return parkingLots.findById(id)
                .orElseThrow(() -> new ParkingLotNotFoundException("No parking lot found for id '" + id + "'"));
    }

    public List<ParkingLot> getAll() {
        return parkingLots.findAll();
    }

    /**
     * @return every currently available spot in the lot, paired with its
     *         floor number, in floor/spot order
     */
    public List<AvailableSpotView> getAvailability(String lotId) {
        ParkingLot lot = getById(lotId);
        return lot.getFloors().stream()
                .flatMap(floor -> floor.getSpots().stream()
                        .filter(spot -> spot.getStatus() == SpotStatus.AVAILABLE)
                        .map(spot -> new AvailableSpotView(spot.getId(), floor.getFloorNumber(),
                                spot.getSpotNumber(), spot.getSpotType())))
                .toList();
    }
}
