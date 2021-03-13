package com.himadri.tnt.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ShipmentApiResponse implements ApiResponse {
    @JsonValue
    private Map<OrderNumber, List<Product>> orderNumberToProductListMap;

    @JsonCreator
    public ShipmentApiResponse(Map<OrderNumber, List<Product>> orderNumberToProductListMap) {
        this.orderNumberToProductListMap = orderNumberToProductListMap;
    }

    @Override
    public ApiResponse filterOnParams(List<String> params) {
        Set<String> paramSet = new HashSet<>(params);
        return new ShipmentApiResponse(
            orderNumberToProductListMap.entrySet()
                .stream()
                .filter(entry -> paramSet.contains(entry.getKey().getOrderNumber()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }
}

