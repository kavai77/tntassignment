package com.himadri.tnt.integrationtest;

import com.himadri.tnt.AggregationController;
import com.himadri.tnt.entity.AggregationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "INTEGRATIONTEST", matches = "enabled")
public class AggregationControllerTest {
    @Autowired
    private AggregationController aggregationController;

    @Test
    void testEmptyAggregationController() {
        AggregationResponse aggregation = aggregationController.aggregation(null, null, null)
            .block();
        assertNull(aggregation.getPricingApiResponse());
        assertNull(aggregation.getShipmentApiResponse());
        assertNull(aggregation.getTrackApiResponse());
    }

    @Test
    void testPartialAggregationController() {
        AggregationResponse aggregation = aggregationController.aggregation(
            "",
            null,
            "109347266,123456892")
            .block();
        assertNull(aggregation.getPricingApiResponse());
        assertNull(aggregation.getTrackApiResponse());
        assertEquals(2, aggregation.getShipmentApiResponse().getOrderNumberToProductListMap().size());
    }

    @Test
    void testFullAggregationController() {
        AggregationResponse aggregation = aggregationController.aggregation(
            "NL,CN",
            "109347263,123456891",
            "109347266,123456892")
            .block();
        assertEquals(2, aggregation.getPricingApiResponse().getCountryToPriceMap().size());
        assertEquals(2, aggregation.getTrackApiResponse().getOrderNumberToTrackingStatusMap().size());
        assertEquals(2, aggregation.getShipmentApiResponse().getOrderNumberToProductListMap().size());
    }
}
