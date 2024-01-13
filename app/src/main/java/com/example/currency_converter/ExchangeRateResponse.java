package com.example.currency_converter;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ExchangeRateResponse {
    @SerializedName("conversion_rates")
    private Map<String, Double> rates;

    public Map<String, Double> getRates() {
        return rates;
    }
}