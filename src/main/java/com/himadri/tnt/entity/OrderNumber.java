package com.himadri.tnt.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

@Data
public class OrderNumber {
    @JsonValue
    private String orderNumber;

    @JsonCreator
    public OrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
