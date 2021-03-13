package com.himadri.tnt.gateway;

import com.himadri.tnt.entity.ApiResponse;
import com.himadri.tnt.entity.PricingApiResponse;
import com.himadri.tnt.entity.ShipmentApiResponse;
import com.himadri.tnt.entity.TrackApiResponse;
import com.himadri.tnt.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;

@Component
public class DefaultApiGateway implements ApiGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultApiGateway.class);
    private static final Map<Class<? extends ApiResponse>, String> apiToPathMap = Map.of(
        PricingApiResponse.class, "/pricing",
        ShipmentApiResponse.class, "/shipments",
        TrackApiResponse.class, "/track"
    );

    @Value("${baseurl}")
    private String baseurl;

    @Value("${api.timeout.in.sec}")
    private long apiTimeoutInSec;

    private WebClient webClient;

    @PostConstruct
    public void init() {
       webClient = WebClient.builder()
            .baseUrl(baseurl)
            .build();
    }

    @Override
    public <T extends ApiResponse> Mono<T> queryApi(Class<T> apiGatewayClass, String queryParam) {
        if (StringUtils.isEmpty(queryParam)) {
            return Mono.empty();
        }
        String path = apiToPathMap.get(apiGatewayClass);
        return webClient.get()
            .uri(path + "?q={q}", Map.of("q", queryParam))
            .retrieve()
            .bodyToMono(apiGatewayClass)
            .doOnNext(it -> LOGGER.info("Api Response " + apiGatewayClass.getSimpleName()))
            .onErrorMap(WebClientResponseException.class, ApiException::new)
            .timeout(Duration.ofSeconds(apiTimeoutInSec));
    }
}
