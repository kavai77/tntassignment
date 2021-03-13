package com.himadri.tnt.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AggregationResponse {
    @JsonProperty("pricing")
    private PricingApiResponse pricingApiResponse;
    @JsonProperty("track")
    private TrackApiResponse trackApiResponse;
    @JsonProperty("shipments")
    private ShipmentApiResponse shipmentApiResponse;

    public void setApiResponse(ApiResponse apiResponse) {
        if (apiResponse instanceof ShipmentApiResponse) {
            setShipmentApiResponse((ShipmentApiResponse) apiResponse);
        }
        if (apiResponse instanceof TrackApiResponse) {
            setTrackApiResponse((TrackApiResponse) apiResponse);
        }
        if (apiResponse instanceof PricingApiResponse) {
            setPricingApiResponse((PricingApiResponse) apiResponse);
        }
    }
}
