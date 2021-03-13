package com.himadri.tnt.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

@Data
public class Country {
    @JsonValue
    private final String countryIso2;

    @JsonCreator
    public Country(String countryIso2) {
        this.countryIso2 = countryIso2;
    }
}
