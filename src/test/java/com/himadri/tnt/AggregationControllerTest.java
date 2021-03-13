package com.himadri.tnt;

import com.himadri.tnt.entity.AggregationResponse;
import com.himadri.tnt.entity.Country;
import com.himadri.tnt.entity.OrderNumber;
import com.himadri.tnt.entity.PricingApiResponse;
import com.himadri.tnt.entity.Product;
import com.himadri.tnt.entity.ShipmentApiResponse;
import com.himadri.tnt.entity.TrackApiResponse;
import com.himadri.tnt.entity.TrackingStatus;
import com.himadri.tnt.gateway.ApiGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AggregationControllerTest {
    @Mock
    private ApiGateway apiGateway;

    private AggregationController aggregationController;

    @BeforeEach
    void setUp() {
        aggregationController = new AggregationController(apiGateway);
    }

    @Test
    void testSuccessfulAggregate() {
        // GIVEN
        String pricingCountries = "pricing";
        String trackOrderNumbers = "trackOrder";
        String shipmentsOrderNumbers = "shipmentOrder";
        PricingApiResponse pricingApiResponse = new PricingApiResponse(Map.of(new Country("NL"), BigDecimal.ONE));
        TrackApiResponse trackApiResponse = new TrackApiResponse(Map.of(new OrderNumber("123"), TrackingStatus.NEW));
        ShipmentApiResponse shipmentApiResponse = new ShipmentApiResponse(Map.of(new OrderNumber("456"), List.of(new Product("prod"))));
        when(apiGateway.queryApi(eq(PricingApiResponse.class), eq(pricingCountries)))
            .thenReturn(Mono.just(pricingApiResponse));
        when(apiGateway.queryApi(eq(TrackApiResponse.class), eq(trackOrderNumbers)))
            .thenReturn(Mono.just(trackApiResponse));
        when(apiGateway.queryApi(eq(ShipmentApiResponse.class), eq(shipmentsOrderNumbers)))
            .thenReturn(Mono.just(shipmentApiResponse));

        // WHEN
        AggregationResponse aggregation = aggregationController
            .aggregation(pricingCountries, trackOrderNumbers, shipmentsOrderNumbers)
            .block();

        // THEN
        assertEquals(pricingApiResponse, aggregation.getPricingApiResponse());
        assertEquals(trackApiResponse, aggregation.getTrackApiResponse());
        assertEquals(shipmentApiResponse, aggregation.getShipmentApiResponse());
    }

    @Test
    void testFailingMonos() {
        // GIVEN
        String pricingCountries = "pricing";
        String trackOrderNumbers = "trackOrder";
        String shipmentsOrderNumbers = "shipmentOrder";
        PricingApiResponse pricingApiResponse = new PricingApiResponse(Map.of(new Country("NL"), BigDecimal.ONE));
        when(apiGateway.queryApi(eq(PricingApiResponse.class), eq(pricingCountries)))
            .thenReturn(Mono.just(pricingApiResponse));
        when(apiGateway.queryApi(eq(TrackApiResponse.class), eq(trackOrderNumbers)))
            .thenReturn(Mono.error(RuntimeException::new));
        when(apiGateway.queryApi(eq(ShipmentApiResponse.class), eq(shipmentsOrderNumbers)))
            .thenReturn(Mono.empty());

        // WHEN
        AggregationResponse aggregation = aggregationController
            .aggregation(pricingCountries, trackOrderNumbers, shipmentsOrderNumbers)
            .block();

        // THEN
        assertEquals(pricingApiResponse, aggregation.getPricingApiResponse());
        assertNull(aggregation.getTrackApiResponse());
        assertNull(aggregation.getShipmentApiResponse());
    }
}