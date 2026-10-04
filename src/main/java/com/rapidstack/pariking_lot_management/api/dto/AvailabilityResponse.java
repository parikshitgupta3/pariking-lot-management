package com.rapidstack.pariking_lot_management.api.dto;

import com.rapidstack.pariking_lot_management.application.usecase.parkinglot.AvailableSpotView;
import com.rapidstack.pariking_lot_management.domain.enums.SpotType;

import java.util.List;

/**
 * Response for a lot's availability: the count of free spots plus each free
 * spot paired with its floor.
 */
public record AvailabilityResponse(int availableSpotCount, List<AvailableSpotResponse> spots) {

    public record AvailableSpotResponse(int floorNumber, String spotNumber, SpotType spotType) {
    }

    public static AvailabilityResponse from(List<AvailableSpotView> views) {
        return new AvailabilityResponse(
                views.size(),
                views.stream()
                        .map(view -> new AvailableSpotResponse(view.floorNumber(), view.spotNumber(), view.spotType()))
                        .toList());
    }
}
