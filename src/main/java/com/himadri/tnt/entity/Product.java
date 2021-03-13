package com.himadri.tnt.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

@Data
public class Product {
    @JsonValue
    private String product;

    @JsonCreator
    public Product(String product) {
        this.product = product;
    }
}
