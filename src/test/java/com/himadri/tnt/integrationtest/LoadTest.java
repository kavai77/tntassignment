package com.himadri.tnt.integrationtest;

import com.himadri.tnt.AggregationController;
import com.himadri.tnt.entity.AggregationResponse;
import com.himadri.tnt.entity.OrderNumber;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "INTEGRATIONTEST", matches = "enabled")
public class LoadTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadTest.class);

    @Autowired
    private AggregationController aggregationController;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static List<Long> runningTimes = new ArrayList<>();
    private static AtomicInteger assertCount = new AtomicInteger();
    private static AtomicInteger errorCount = new AtomicInteger();

    @AfterAll
    public static void printStatistics() {
        OptionalDouble average = runningTimes
            .stream()
            .mapToLong(it -> it)
            .average();
        LOGGER.info("Running times: {}", runningTimes);
        LOGGER.info("Average running time: {}", average.getAsDouble());
        LOGGER.info("API assert count: {}", assertCount.get());
        LOGGER.info("API error count: {}", errorCount.get());
    }

    @RepeatedTest(10)
    void testFullAggregationController() throws Exception {
        int parallelRequests = RandomUtils.nextInt(1, 5);
        CountDownLatch countDownLatch = new CountDownLatch(parallelRequests);
        List<Executable> assertions = new ArrayList<>();
        for (int i = 0; i < parallelRequests; i++) {
            executor.submit(() -> {
                String countries = Stream.generate(() -> RandomStringUtils.randomAlphabetic(2))
                    .limit(RandomUtils.nextInt(1, 7))
                    .collect(Collectors.joining(","));
                String trackOrderNumbers = Stream.generate(() -> RandomStringUtils.randomNumeric(9))
                    .limit(RandomUtils.nextInt(1, 7))
                    .collect(Collectors.joining(","));
                String shipmentOrderNumbers = Stream.generate(() -> RandomStringUtils.randomNumeric(9))
                    .limit(RandomUtils.nextInt(1, 7))
                    .collect(Collectors.joining(","));
                long startTime = System.currentTimeMillis();
                try {
                    AggregationResponse response = aggregationController.aggregation(
                        countries,
                        trackOrderNumbers,
                        shipmentOrderNumbers)
                        .block();
                    if(response.getShipmentApiResponse() != null) {
                        assertions.add( () ->
                            assertEquals(
                                Arrays.stream(shipmentOrderNumbers.split(",")).collect(Collectors.toSet()),
                                response.getShipmentApiResponse().getOrderNumberToProductListMap().keySet().stream().map(OrderNumber::getOrderNumber).collect(Collectors.toSet())
                            )
                        );
                    } else {
                        errorCount.incrementAndGet();
                    }
                    if(response.getTrackApiResponse() != null) {
                        assertions.add( () ->
                            assertEquals(
                                Arrays.stream(trackOrderNumbers.split(",")).collect(Collectors.toSet()),
                                response.getTrackApiResponse().getOrderNumberToTrackingStatusMap().keySet().stream().map(OrderNumber::getOrderNumber).collect(Collectors.toSet())
                            )
                        );
                    } else {
                        errorCount.incrementAndGet();
                    }
                    if(response.getTrackApiResponse() != null) {
                        assertions.add( () ->
                            assertEquals(
                                Arrays.stream(trackOrderNumbers.split(",")).collect(Collectors.toSet()),
                                response.getTrackApiResponse().getOrderNumberToTrackingStatusMap().keySet().stream().map(OrderNumber::getOrderNumber).collect(Collectors.toSet())
                            )
                        );
                    } else {
                        errorCount.incrementAndGet();
                    }
                } finally {
                    runningTimes.add(System.currentTimeMillis() - startTime);
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        assertAll(assertions);
        assertCount.addAndGet(assertions.size());
    }
}
