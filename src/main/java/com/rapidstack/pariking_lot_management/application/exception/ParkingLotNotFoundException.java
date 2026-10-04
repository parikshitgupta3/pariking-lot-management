package com.rapidstack.pariking_lot_management.application.exception;

/**
 * Thrown when an operation references a parking lot id that no lot exists
 * for. A lookup failure rather than a domain-rule violation, hence it lives
 * with the application layer.
 */
public class ParkingLotNotFoundException extends RuntimeException {

    public ParkingLotNotFoundException(String message) {
        super(message);
    }
}
