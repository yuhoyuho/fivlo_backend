package com.fivlo.fivlo_backend.domain.reminder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record AddressDto(
        @JsonProperty("address_name")
        String addressName,
        @JsonProperty("road_address")
        String roadAddress,
        BigDecimal x,
        BigDecimal y
) {
}
