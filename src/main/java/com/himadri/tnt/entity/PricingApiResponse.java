package com.himadri.tnt.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class PricingApiResponse implements ApiResponse {
    @JsonValue
    private final Map<Country, BigDecimal> countryToPriceMap;

    @JsonCreator
    public PricingApiResponse(Map<Country, BigDecimal> countryToPriceMap) {
        this.countryToPriceMap = countryToPriceMap;
    }

    @Override
    public ApiResponse filterOnParams(List<String> params) {
        Set<String> paramSet = new HashSet<>(params);
        return new PricingApiResponse(
            countryToPriceMap.entrySet()
                .stream()
                .filter(entry -> paramSet.contains(entry.getKey().getCountryIso2()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }
}
