package com.himadri.tnt.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class TrackApiResponse implements ApiResponse {
    @JsonValue
    private final Map<OrderNumber, TrackingStatus> orderNumberToTrackingStatusMap;

    @JsonCreator
    public TrackApiResponse(Map<OrderNumber, TrackingStatus> orderNumberToTrackingStatusMap) {
        this.orderNumberToTrackingStatusMap = orderNumberToTrackingStatusMap;
    }

    @Override
    public ApiResponse filterOnParams(List<String> params) {
        Set<String> paramSet = new HashSet<>(params);
        return new TrackApiResponse(
            orderNumberToTrackingStatusMap.entrySet()
                .stream()
                .filter(entry -> paramSet.contains(entry.getKey().getOrderNumber()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }
}
