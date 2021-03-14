package com.himadri.tnt.gateway;

import com.himadri.tnt.entity.ApiResponse;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Primary
public class ThrottlingApiGateway implements ApiGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThrottlingApiGateway.class);

    private final DefaultApiGateway defaultApiGateway;
    private final int bufferSize;
    private final int bufferTimeoutInSec;
    private final long apiTimeoutInSec;
    private final ConcurrentMap<Class<?>, Buffer<?>> apiQueryBuffer = new ConcurrentHashMap<>();

    private FluxSink<Long> schedulerPushSink;

    public ThrottlingApiGateway(
        DefaultApiGateway defaultApiGateway,
        @Value("${buffer.size}") int bufferSize,
        @Value("${buffer.timeout.in.sec}") int bufferTimeoutInSec,
        @Value("${api.timeout.in.sec}") long apiTimeoutInSec
    ) {
        this.defaultApiGateway = defaultApiGateway;
        this.bufferSize = bufferSize;
        this.bufferTimeoutInSec = bufferTimeoutInSec;
        this.apiTimeoutInSec = apiTimeoutInSec;
    }

    @PostConstruct
    public void scheduler() {
        Flux<Long> schedulerBackPressureMono = Flux.create(sink -> {
            this.schedulerPushSink = sink;
        });
        Flux.interval(Duration.ofSeconds(1))
            .mergeWith(schedulerBackPressureMono)
            .doOnNext(tick -> {
                LOGGER.debug("tick {}", tick);
                List<Mono<? extends ApiResponse>> monoList = new ArrayList<>();
                for (Iterator<Class<?>> it = apiQueryBuffer.keySet().iterator(); it.hasNext(); ) {
                    Class<?> apiGatewayClass = it.next();
                    synchronized (apiGatewayClass) {
                        Buffer<?> buffer = apiQueryBuffer.get(apiGatewayClass);
                        if (buffer.getParamList().size() >= bufferSize
                                || System.currentTimeMillis() - buffer.getCreationTime() >= bufferTimeoutInSec * 1000L) {
                            it.remove();
                            monoList.add(buffer.triggerQueryApi());
                        }
                    }
                }
                Flux.merge(monoList).subscribe();
            })
            .subscribe();
    }

    @Override
    public <T extends ApiResponse> Mono<T> queryApi(Class<T> apiGatewayClass, String queryParam) {
        LOGGER.info("Receiving request {} with params {}", apiGatewayClass.getSimpleName(), queryParam);
        if (StringUtils.isEmpty(queryParam)) {
            return Mono.empty();
        }
        List<String> params = Arrays.asList(queryParam.split(","));
        return Mono.<T>create(
            sink -> {
                synchronized (apiGatewayClass) {
                    Buffer<T> buffer = (Buffer<T>) apiQueryBuffer.computeIfAbsent(apiGatewayClass, it -> new Buffer<>(apiGatewayClass));
                    buffer.getParamList().addAll(params);
                    buffer.getSinks().add(sink);
                    LOGGER.info("Buffering {}: {}. Buffer size: {} Sink size: {}",
                        apiGatewayClass.getSimpleName(), params.toString(), buffer.getParamList().size(), buffer.getSinks().size());
                    if (buffer.getParamList().size() >= bufferSize) {
                        schedulerPushSink.next(0L);
                    }
                }
            })
            .timeout(Duration.ofSeconds(apiTimeoutInSec))
            .map(it -> (T) it.filterOnParams(params));
    }

    @Data
    private class Buffer<T extends ApiResponse> {
        private final Class<T> apiGatewayClass;
        private final long creationTime = System.currentTimeMillis();
        private final Set<String> paramList = new HashSet<>();
        private final List<MonoSink<T>> sinks = new ArrayList<>();

        public Mono<T> triggerQueryApi() {
            LOGGER.info("Triggering API query: {}", apiGatewayClass.getSimpleName());
            return defaultApiGateway.queryApi(apiGatewayClass, StringUtils.collectionToCommaDelimitedString(paramList))
                .doOnError(error -> {
                    sinks.forEach(it -> {
                        it.error(error);
                    });
                })
                .doOnNext(response -> {
                    sinks.forEach(it -> {
                        it.success(response);
                    });
                });
        }
    }
}
