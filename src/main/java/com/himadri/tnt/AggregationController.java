package com.himadri.tnt;

import com.himadri.tnt.entity.AggregationResponse;
import com.himadri.tnt.entity.PricingApiResponse;
import com.himadri.tnt.entity.ShipmentApiResponse;
import com.himadri.tnt.entity.TrackApiResponse;
import com.himadri.tnt.exception.ApiException;
import com.himadri.tnt.gateway.ApiGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@RestController
public class AggregationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationController.class);

    private final ApiGateway apiGateway;

    @Autowired
    public AggregationController(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    @GetMapping("/aggregation")
    @ResponseBody
    public Mono<AggregationResponse> aggregation(
        @RequestParam(name = "pricing", required = false) String pricingCountries,
        @RequestParam(name = "track", required = false) String trackOrderNumbers,
        @RequestParam(name = "shipments", required = false) String shipmentsOrderNumbers
    ) {
        Mono<PricingApiResponse> pricing = apiGateway.queryApi(PricingApiResponse.class, pricingCountries);
        Mono<TrackApiResponse> track = apiGateway.queryApi(TrackApiResponse.class, trackOrderNumbers);
        Mono<ShipmentApiResponse> shipment = apiGateway.queryApi(ShipmentApiResponse.class, shipmentsOrderNumbers);

        return Flux.merge(pricing, track, shipment)
            .onErrorResume(ApiException .class, (ex) -> {LOGGER.warn("Api Error", ex);return Mono.empty();})
            .onErrorResume(TimeoutException.class, (ex) -> {LOGGER.warn("Api Timeout", ex);return Mono.empty();})
            .onErrorResume(Throwable.class, (ex) -> {LOGGER.warn("Unknown error", ex);return Mono.empty();})
            .collect(AggregationResponse::new, AggregationResponse::setApiResponse);
    }
}
