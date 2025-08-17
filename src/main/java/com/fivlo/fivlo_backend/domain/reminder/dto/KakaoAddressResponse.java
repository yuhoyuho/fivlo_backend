package com.fivlo.fivlo_backend.domain.reminder.dto;

import java.util.List;

public record KakaoAddressResponse(
        List<Document> documents
) {
}
