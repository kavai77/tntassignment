package com.himadri.tnt.gateway;

import com.himadri.tnt.entity.ApiResponse;
import reactor.core.publisher.Mono;

public interface ApiGateway {
    <T extends ApiResponse> Mono<T> queryApi(Class<T> apiGatewayClass, String queryParam);
}
