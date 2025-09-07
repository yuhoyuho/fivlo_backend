package com.fivlo.fivlo_backend.domain.reminder.dto;

public record Document(
        String addressName,
        String roadAddressName,
        String x, // 카카오는 좌표를 문자열로 제공해줌
        String y
) {
}
