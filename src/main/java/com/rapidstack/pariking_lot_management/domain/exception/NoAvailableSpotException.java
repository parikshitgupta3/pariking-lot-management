package com.rapidstack.pariking_lot_management.domain.exception;

/**
 * Thrown when a vehicle cannot be admitted because no available spot in the
 * lot fits it. Carries the offending vehicle and lot in the message so a
 * caller (e.g. a future REST layer) can surface a meaningful response.
 */
public class NoAvailableSpotException extends RuntimeException {

    public NoAvailableSpotException(String message) {
        super(message);
    }
}
