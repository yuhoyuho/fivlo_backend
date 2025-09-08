-- V2__update_time_attack_goals_schema.sql
-- TimeAttackGoal 엔티티 업데이트에 따른 스키마 변경
-- 기존 name 컬럼을 name_key와 custom_name으로 분리하여 다국어 지원

-- ==================== 새 컬럼 추가 ====================

-- 미리 정의된 목적의 i18n 키 (예: "timeAttack.goal.outingPrep")
-- isPredefined가 true일 때만 사용
ALTER TABLE time_attack_goals
ADD COLUMN name_key VARCHAR(100);

-- 사용자가 직접 추가한 목적의 이름
-- isPredefined가 false일 때만 사용
ALTER TABLE time_attack_goals
ADD COLUMN custom_name VARCHAR(255);

-- ==================== 기존 데이터 마이그레이션 ====================

-- 기존 데이터를 새로운 컬럼 구조로 마이그레이션
-- 사용자 생성 목적 (is_predefined = false): name → custom_name
UPDATE time_attack_goals 
SET custom_name = name 
WHERE is_predefined = false;

-- 미리 정의된 목적 (is_predefined = true): name → name_key
UPDATE time_attack_goals 
SET name_key = name 
WHERE is_predefined = true;

-- ==================== 구버전 컬럼 제거 ====================

-- 기존 name 컬럼 제거 (데이터 마이그레이션 완료 후)
ALTER TABLE time_attack_goals 
DROP COLUMN name;

-- ==================== 인덱스 최적화 ====================

-- 자주 조회되는 컬럼들에 대한 인덱스 추가
CREATE INDEX idx_time_attack_goals_name_key ON time_attack_goals(name_key) WHERE name_key IS NOT NULL;
CREATE INDEX idx_time_attack_goals_custom_name ON time_attack_goals(custom_name) WHERE custom_name IS NOT NULL;
