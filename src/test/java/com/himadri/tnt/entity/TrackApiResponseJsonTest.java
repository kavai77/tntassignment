package com.himadri.tnt.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackApiResponseJsonTest {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testConvertToJson() throws Exception {
        Map<OrderNumber, TrackingStatus> map = new LinkedHashMap<>();
        map.put(new OrderNumber("109347263"), TrackingStatus.NEW);
        map.put(new OrderNumber("123456891"), TrackingStatus.IN_TRANSIT);
        TrackApiResponse shipment = new TrackApiResponse(map);
        String actualJson = objectMapper.writeValueAsString(shipment);
        String expectedJson = "{\"109347263\":\"NEW\",\"123456891\":\"IN TRANSIT\"}";;
        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testConvertFromJson() throws Exception {
        String json = "{\"109347263\": \"NEW\",\"123456891\": \"IN TRANSIT\"}";
        TrackApiResponse actualTracking = objectMapper.readValue(json, TrackApiResponse.class);
        TrackApiResponse expectedTracking = new TrackApiResponse(
            Map.of(
                new OrderNumber("109347263"), TrackingStatus.NEW,
                new OrderNumber("123456891"), TrackingStatus.IN_TRANSIT
            )
        );
        assertEquals(expectedTracking, actualTracking);
    }

}