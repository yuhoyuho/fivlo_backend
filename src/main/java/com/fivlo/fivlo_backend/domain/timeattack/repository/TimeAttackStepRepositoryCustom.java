package com.fivlo.fivlo_backend.domain.timeattack.repository;

public interface TimeAttackStepRepositoryCustom {
    /** 세션 내 스텝 duration 합계(초). 스텝 없으면 0 */
    int sumDurationBySessionId(Long sessionId);
}

