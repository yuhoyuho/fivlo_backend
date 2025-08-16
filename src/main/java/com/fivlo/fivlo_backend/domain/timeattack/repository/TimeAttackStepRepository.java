package com.fivlo.fivlo_backend.domain.timeattack.repository;

import com.fivlo.fivlo_backend.domain.timeattack.entity.TimeAttackStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeAttackStepRepository
        extends JpaRepository<TimeAttackStep, Long>, TimeAttackStepRepositoryCustom {

    // 세션별 스텝 정렬 조회
    List<TimeAttackStep> findByTimeAttackSession_IdOrderByStepOrderAsc(Long timeAttackSessionId);

    long countByTimeAttackSession_Id(Long timeAttackSessionId);

    // ✅ MAX 대체: 가장 큰 stepOrder 가진 레코드 1건을 정렬로 가져오기
    Optional<TimeAttackStep> findTopByTimeAttackSession_IdOrderByStepOrderDesc(Long timeAttackSessionId);

    // 특정 순서
    Optional<TimeAttackStep> findByTimeAttackSession_IdAndStepOrder(Long timeAttackSessionId, Integer stepOrder);

    // 세션 삭제 시 단계 일괄 삭제
    void deleteByTimeAttackSession_Id(Long timeAttackSessionId);

    // N+1 완화: 여러 세션의 스텝을 세션/순서 기준으로 한 번에 조회
    List<TimeAttackStep> findByTimeAttackSession_IdInOrderByTimeAttackSession_IdAscStepOrderAsc(List<Long> sessionIds);
}
