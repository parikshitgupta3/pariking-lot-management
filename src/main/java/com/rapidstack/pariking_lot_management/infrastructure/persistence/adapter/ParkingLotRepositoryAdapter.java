package com.rapidstack.pariking_lot_management.infrastructure.persistence.adapter;

import com.rapidstack.pariking_lot_management.application.port.ParkingLotRepository;
import com.rapidstack.pariking_lot_management.domain.model.ParkingLot;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.entity.ParkingLotEntity;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.mapper.ParkingLotMapper;
import com.rapidstack.pariking_lot_management.infrastructure.persistence.repository.ParkingLotJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * The JPA-backed implementation of the application's
 * {@link ParkingLotRepository} port. Saving persists the whole aggregate
 * (floors, spots) by cascade and returns the reloaded domain state, so
 * generated ordering and constraints are reflected back to the caller.
 */
@Repository
public class ParkingLotRepositoryAdapter implements ParkingLotRepository {

    private final ParkingLotJpaRepository lots;

    public ParkingLotRepositoryAdapter(ParkingLotJpaRepository lots) {
        this.lots = lots;
    }

    @Override
    @Transactional
    public ParkingLot save(ParkingLot lot) {
        ParkingLotEntity saved = lots.save(ParkingLotMapper.toEntity(lot));
        return ParkingLotMapper.toDomain(saved);
    }

    @Override
    public Optional<ParkingLot> findById(String id) {
        return lots.findById(id)
                .map(ParkingLotMapper::toDomain);
    }

    @Override
    public List<ParkingLot> findAll() {
        return lots.findAll().stream()
                .map(ParkingLotMapper::toDomain)
                .toList();
    }
}
