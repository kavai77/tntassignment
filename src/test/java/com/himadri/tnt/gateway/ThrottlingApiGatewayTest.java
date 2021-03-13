package com.himadri.tnt.gateway;

import com.himadri.tnt.entity.PricingApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ThrottlingApiGatewayTest {
    @Mock
    private DefaultApiGateway defaultApiGateway;

    private ThrottlingApiGateway throttlingApiGateway;

    @BeforeEach
    void setUp() {
        lenient().when(defaultApiGateway.queryApi(eq(PricingApiResponse.class), anyString()))
            .thenReturn(Mono.just(new PricingApiResponse(Map.of())));

        throttlingApiGateway = new ThrottlingApiGateway(
            defaultApiGateway,
            5,
            Integer.MAX_VALUE,
            1
        );
        throttlingApiGateway.scheduler();
    }

    @Test
    void nullQuery() {
        // WHEN
        PricingApiResponse pricingApiResponse = throttlingApiGateway.queryApi(PricingApiResponse.class, null)
            .block();

        // THEN
        assertNull(pricingApiResponse);

        // THEN
        verifyNoInteractions(defaultApiGateway);
    }

    @Test
    void emptyQuery() {
        // WHEN
        PricingApiResponse pricingApiResponse = throttlingApiGateway.queryApi(PricingApiResponse.class, "")
            .block();

        // THEN
        assertNull(pricingApiResponse);

        // THEN
        verifyNoInteractions(defaultApiGateway);
    }

    @Test
    void notEnoughParameter_timout() {
        // WHEN
        assertThrows(Exception.class, () ->
            throttlingApiGateway.queryApi(PricingApiResponse.class, "1,2,3,4").block());

        // THEN
        verifyNoInteractions(defaultApiGateway);
    }

    @Test
    void bufferingInMultipleGoes() {
        // WHEN
        throttlingApiGateway.queryApi(PricingApiResponse.class, "1,2").subscribe();
        throttlingApiGateway.queryApi(PricingApiResponse.class, "3,4").subscribe();
        throttlingApiGateway.queryApi(PricingApiResponse.class, "5").subscribe();

        // THEN
        verify(defaultApiGateway, times(1)).queryApi(PricingApiResponse.class, "1,2,3,4,5");
    }

    @Test
    void enoughParameterNoBuffer() {
        // WHEN
        throttlingApiGateway.queryApi(PricingApiResponse.class, "1,2,3,4,5").subscribe();

        // THEN
        verify(defaultApiGateway, times(1)).queryApi(PricingApiResponse.class, "1,2,3,4,5");
    }
}