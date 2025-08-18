package com.fivlo.fivlo_backend.domain.reminder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Document(
        @JsonProperty("address_name")
        String addressName,
        @JsonProperty("road_address_name")
        String roadAddressName,
        String x, // 카카오는 좌표를 문자열로 제공해줌
        String y
) {
}
