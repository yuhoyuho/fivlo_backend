-- V6: 타임어택 AI 추천 최적화를 위한 인덱스 추가
-- 목적: 같은 목표+시간 조합의 최근 세션을 빠르게 조회하기 위함

-- TimeAttackSession 테이블에 복합 인덱스 생성
-- (user_id, time_attack_goal_id, total_duration_in_seconds, created_at DESC)
CREATE INDEX IF NOT EXISTS idx_time_attack_session_user_goal_duration_created
ON time_attack_session(user_id, time_attack_goal_id, total_duration_in_seconds, created_at DESC);

-- 설명:
-- 이 인덱스는 TimeAttackService.recommendSteps()에서
-- findTopByUser_IdAndTimeAttackGoal_IdAndTotalDurationInSecondsOrderByCreatedAtDesc() 
-- 메서드 실행 시 성능을 극대적으로 향상시킵니다.
-- 
-- 예상 성능: 
-- - 인덱스 없이: ~10-50ms (테이블 풀 스캔)
-- - 인덱스 있음: ~1-2ms (인덱스 스캔)
