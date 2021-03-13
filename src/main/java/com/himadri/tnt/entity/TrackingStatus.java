package com.himadri.tnt.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum TrackingStatus {
    NEW("NEW"),
    IN_TRANSIT("IN TRANSIT"),
    COLLECTING("COLLECTING"),
    COLLECTED("COLLECTED"),
    DELIVERING("DELIVERING"),
    DELIVERED("DELIVERED");

    private final String status;

    TrackingStatus(String status) {
        this.status = status;
    }

    @JsonValue
    public String getStatus() {
        return status;
    }

    @JsonCreator
    public static TrackingStatus findByStatus(String status) {
        return Arrays.stream(TrackingStatus.values())
            .filter(it -> it.getStatus().equals(status))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
