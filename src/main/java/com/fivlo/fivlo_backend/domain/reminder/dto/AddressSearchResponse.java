package com.fivlo.fivlo_backend.domain.reminder.dto;

import java.util.List;

public record AddressSearchResponse(
        List<AddressDto> addresses
) {
}
