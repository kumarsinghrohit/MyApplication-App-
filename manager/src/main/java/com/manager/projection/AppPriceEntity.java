package com.manager.projection;

import java.util.Currency;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(value = AccessLevel.PACKAGE)
class AppPriceEntity {

    private Currency currency;
    private double value;

    @JsonCreator
    public AppPriceEntity(@JsonProperty("currency") Currency currency, @JsonProperty("value") double value) {
        this.currency = currency;
        this.value = value;
    }
}
