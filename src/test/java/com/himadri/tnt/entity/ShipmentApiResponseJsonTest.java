package com.himadri.tnt.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipmentApiResponseJsonTest {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testConvertToJson() throws Exception {
        Map<OrderNumber, List<Product>> map = new LinkedHashMap<>();
        map.put(new OrderNumber("109347263"), List.of(new Product("box"), new Product("box"), new Product("pallet")));
        map.put(new OrderNumber("123456891"), List.of(new Product("envelope")));
        ShipmentApiResponse shipment = new ShipmentApiResponse(map);
        String actualJson = objectMapper.writeValueAsString(shipment);
        String expectedJson = "{\"109347263\":[\"box\",\"box\",\"pallet\"],\"123456891\":[\"envelope\"]}";
        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testConvertFromJson() throws Exception {
        String json = "{\"109347263\":[\"box\",\"box\",\"pallet\"],\"123456891\":[\"envelope\"]}";
        ShipmentApiResponse actualShipment = objectMapper.readValue(json, ShipmentApiResponse.class);
        ShipmentApiResponse expectedShipment = new ShipmentApiResponse(
            Map.of(
                new OrderNumber("109347263"), List.of(new Product("box"), new Product("box"), new Product("pallet")),
                new OrderNumber("123456891"), List.of(new Product("envelope"))
            )
        );
        assertEquals(expectedShipment, actualShipment);
    }

}