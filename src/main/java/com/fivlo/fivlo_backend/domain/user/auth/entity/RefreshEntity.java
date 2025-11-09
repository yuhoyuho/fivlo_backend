package com.fivlo.fivlo_backend.domain.user.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 30 * 3) // 3개월 유지
public class RefreshEntity {

    @Id
    private Long userId; // userId를 key로 사용

    @Indexed
    private String token;
}
