-- V8__Update_concentration_goals_for_daily_targets.sql
-- D-Day 기능을 하루 단위 목표로 수정하고, 달성율 계산을 위한 목표 시간 추가
-- 작성일: 2026-01-21

-- ==================== concentration_goals 테이블 수정 ====================

-- 1. 목표 집중 시간 컬럼 추가 (달성율 계산을 위해 필수)
ALTER TABLE concentration_goals
    ADD COLUMN target_focus_time_in_seconds INTEGER NOT NULL DEFAULT 7200;

COMMENT ON COLUMN concentration_goals.target_focus_time_in_seconds IS '하루 목표 집중 시간 (초 단위, 기본값: 7200초 = 2시간)';

-- 2. end_date 컬럼 삭제 (하루 목표만 지원)
ALTER TABLE concentration_goals
    DROP COLUMN end_date;

-- 3. start_date를 target_date로 이름 변경
ALTER TABLE concentration_goals
    RENAME COLUMN start_date TO target_date;

COMMENT ON COLUMN concentration_goals.target_date IS 'D-Day 목표 날짜 (하루 단위)';

-- 4. 기존 인덱스 삭제 및 새 인덱스 생성
DROP INDEX IF EXISTS idx_concentration_goals_dates;
CREATE INDEX idx_concentration_goals_target_date ON concentration_goals(target_date);
CREATE INDEX idx_concentration_goals_user_target_date ON concentration_goals(user_id, target_date);

-- ==================== pomodoro_sessions 테이블 수정 ====================

-- 5. D-Day 목표와 연결하기 위한 컬럼 추가
ALTER TABLE pomodoro_sessions
    ADD COLUMN concentration_goal_id BIGINT REFERENCES concentration_goals(id) ON DELETE SET NULL;

COMMENT ON COLUMN pomodoro_sessions.concentration_goal_id IS 'D-Day 목표와 연결 (NULL이면 일반 포모도로 세션)';

-- 6. concentration_goal_id에 대한 인덱스 생성
CREATE INDEX idx_pomodoro_sessions_concentration_goal_id
    ON pomodoro_sessions(concentration_goal_id);

-- 7. user_id와 concentration_goal_id 복합 인덱스 (D-Day 분석 성능 향상)
CREATE INDEX idx_pomodoro_sessions_user_concentration_goal
    ON pomodoro_sessions(user_id, concentration_goal_id)
    WHERE concentration_goal_id IS NOT NULL;

-- ==================== 마이그레이션 완료 ====================
-- 변경 사항 요약:
-- 1. concentration_goals.target_focus_time_in_seconds 추가 (달성율 계산용)
-- 2. concentration_goals.end_date 삭제 (하루 목표만 지원)
-- 3. concentration_goals.start_date → target_date 변경
-- 4. pomodoro_sessions.concentration_goal_id 추가 (D-Day 목표 연결)
-- 5. 관련 인덱스 재생성 및 최적화
