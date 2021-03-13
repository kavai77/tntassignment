package com.himadri.tnt.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingApiResponseJsonTest {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testConvertToJson() throws Exception {
        Map<Country, BigDecimal> map = new LinkedHashMap<>();
        map.put(new Country("NL"), new BigDecimal("14.242090605778"));
        map.put(new Country("CN"), new BigDecimal("20.503467806384"));
        PricingApiResponse shipment = new PricingApiResponse(map);
        String actualJson = objectMapper.writeValueAsString(shipment);
        String expectedJson = "{\"NL\":14.242090605778,\"CN\":20.503467806384}";;
        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testConvertFromJson() throws Exception {
        String json = "{\"NL\": 14.242090605778, \"CN\": 20.503467806384}";
        PricingApiResponse actualPricing = objectMapper.readValue(json, PricingApiResponse.class);
        PricingApiResponse expectedPricing = new PricingApiResponse(
            Map.of(
                new Country("NL"), new BigDecimal("14.242090605778"),
                new Country("CN"), new BigDecimal("20.503467806384")
            )
        );
        assertEquals(expectedPricing, actualPricing);
    }

}