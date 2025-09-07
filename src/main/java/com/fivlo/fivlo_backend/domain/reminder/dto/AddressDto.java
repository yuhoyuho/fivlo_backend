package com.fivlo.fivlo_backend.domain.reminder.dto;

import java.math.BigDecimal;

public record AddressDto(
        String addressName,
        String roadAddress,
        BigDecimal x,
        BigDecimal y
) {
}
