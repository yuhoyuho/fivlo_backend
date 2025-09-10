-- V1__Create_initial_tables.sql
-- FIVLO 앱 초기 테이블 생성
-- 작성일: 2025-08-12

-- ==================== 사용자 테이블 ====================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    social_id VARCHAR(255),
    social_provider VARCHAR(50),
    nickname VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(255),
    onboarding_type VARCHAR(50),
    is_premium BOOLEAN NOT NULL DEFAULT false,
    total_coins INTEGER NOT NULL DEFAULT 0,
    last_pomodoro_coin_date DATE,
    lst_reminder_coin_date DATE,
    last_attendance_coin_date DATE,
    last_login DATE,
    last_task_coin_date DATE,
    fcm_token VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 테이블 인덱스
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_social ON users(social_id, social_provider);

-- ==================== 코인 거래 테이블 ====================
CREATE TABLE coin_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount INTEGER NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 코인 거래 테이블 인덱스
CREATE INDEX idx_coin_transactions_user_id ON coin_transactions(user_id);
CREATE INDEX idx_coin_transactions_date ON coin_transactions(transaction_date);

-- ==================== 카테고리 테이블 ====================
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 카테고리 테이블 인덱스
CREATE INDEX idx_categories_user_id ON categories(user_id);

-- ==================== Task 테이블 ====================
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    content TEXT NOT NULL,
    due_date DATE NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT false,
    repeat_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
    end_date DATE,
    is_linked_to_growth_album BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Task 테이블 인덱스
CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_category_id ON tasks(category_id);
CREATE INDEX idx_tasks_user_due_date ON tasks(user_id, due_date);

-- ==================== 성장앨범 테이블 ====================
CREATE TABLE growth_albums (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT UNIQUE NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    photo_url VARCHAR(255) NOT NULL,
    memo TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 성장앨범 테이블 인덱스
CREATE INDEX idx_growth_albums_task_id ON growth_albums(task_id);
CREATE INDEX idx_growth_albums_created_at ON growth_albums(created_at);

-- ==================== 포모도로 목표 테이블 ====================
CREATE TABLE pomodoro_goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 포모도로 목표 테이블 인덱스
CREATE INDEX idx_pomodoro_goals_user_id ON pomodoro_goals(user_id);

-- ==================== 포모도로 세션 테이블 ====================
CREATE TABLE pomodoro_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pomodoro_goal_id BIGINT NOT NULL REFERENCES pomodoro_goals(id) ON DELETE CASCADE,
    duration_in_seconds INTEGER NOT NULL,
    is_cycle_completed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 포모도로 세션 테이블 인덱스
CREATE INDEX idx_pomodoro_sessions_user_id ON pomodoro_sessions(user_id);
CREATE INDEX idx_pomodoro_sessions_goal_id ON pomodoro_sessions(pomodoro_goal_id);
CREATE INDEX idx_pomodoro_sessions_created_at ON pomodoro_sessions(created_at);

-- ==================== 집중도 목표 테이블 (D-Day) ====================
CREATE TABLE concentration_goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 집중도 목표 테이블 인덱스
CREATE INDEX idx_concentration_goals_user_id ON concentration_goals(user_id);
CREATE INDEX idx_concentration_goals_dates ON concentration_goals(start_date, end_date);

-- ==================== 오분이 아이템 테이블 ====================
CREATE TABLE obooni_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price INTEGER NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    item_type VARCHAR(20) NOT NULL
);

-- 오분이 아이템 테이블 인덱스
CREATE INDEX idx_obooni_items_type ON obooni_items(item_type);
CREATE INDEX idx_obooni_items_price ON obooni_items(price);

-- ==================== 사용자 아이템 테이블 ====================
CREATE TABLE user_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    obooni_item_id BIGINT NOT NULL REFERENCES obooni_items(id) ON DELETE CASCADE,
    is_equipped BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(user_id, obooni_item_id)
);

-- 사용자 아이템 테이블 인덱스
CREATE INDEX idx_user_items_user_id ON user_items(user_id);
CREATE INDEX idx_user_items_equipped ON user_items(user_id, is_equipped);

-- ==================== 타임어택 목표 테이블 ====================
CREATE TABLE time_attack_goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name_key VARCHAR(255) NOT NULL,
    custom_name VARCHAR(255),
    is_predefined BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 타임어택 목표 테이블 인덱스
CREATE INDEX idx_time_attack_goals_user_id ON time_attack_goals(user_id);
CREATE INDEX idx_time_attack_goals_predefined ON time_attack_goals(is_predefined);

-- ==================== 타임어택 세션 테이블 ====================
CREATE TABLE time_attack_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    time_attack_goal_id BIGINT NOT NULL REFERENCES time_attack_goals(id) ON DELETE CASCADE,
    total_duration_in_seconds INTEGER NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 타임어택 세션 테이블 인덱스
CREATE INDEX idx_time_attack_sessions_user_id ON time_attack_sessions(user_id);
CREATE INDEX idx_time_attack_sessions_goal_id ON time_attack_sessions(time_attack_goal_id);
CREATE INDEX idx_time_attack_sessions_created_at ON time_attack_sessions(created_at);

-- ==================== 타임어택 단계 테이블 ====================
CREATE TABLE time_attack_steps (
    id BIGSERIAL PRIMARY KEY,
    time_attack_session_id BIGINT NOT NULL REFERENCES time_attack_sessions(id) ON DELETE CASCADE,
    step_order INTEGER NOT NULL,
    content VARCHAR(255) NOT NULL,
    duration_in_seconds INTEGER NOT NULL
);

-- 타임어택 단계 테이블 인덱스
CREATE INDEX idx_time_attack_steps_session_id ON time_attack_steps(time_attack_session_id);
CREATE INDEX idx_time_attack_steps_order ON time_attack_steps(time_attack_session_id, step_order);

-- ==================== PostGIS 확장 활성화 ====================
-- PostGIS 확장이 필요한 경우 (위치 기반 알림용)
-- CREATE EXTENSION IF NOT EXISTS postgis;

-- ==================== 망각방지 알림 테이블 ====================
CREATE TABLE forgetting_prevention_reminders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    alarm_time TIME NOT NULL,
    repetition_days VARCHAR(7) NOT NULL DEFAULT '',
    location_name VARCHAR(255),
    location_address VARCHAR(255),
    location_latitude DECIMAL(10, 8),
    location_longitude DECIMAL(11, 8),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 망각방지 알림 테이블 인덱스
CREATE INDEX idx_forgetting_prevention_reminders_user_id ON forgetting_prevention_reminders(user_id);
CREATE INDEX idx_forgetting_prevention_reminders_alarm_time ON forgetting_prevention_reminders(alarm_time);
CREATE INDEX idx_forgetting_prevention_reminders_location ON forgetting_prevention_reminders(location_latitude, location_longitude);

-- ==================== 일일 알림 완료 테이블 ====================
CREATE TABLE daily_reminder_completions (
    id BIGSERIAL PRIMARY KEY,
    reminder_id BIGINT NOT NULL REFERENCES forgetting_prevention_reminders(id) ON DELETE CASCADE,
    completion_date DATE NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(reminder_id, completion_date)
);

-- 일일 알림 완료 테이블 인덱스
CREATE INDEX idx_daily_reminder_completions_reminder_id ON daily_reminder_completions(reminder_id);
CREATE INDEX idx_daily_reminder_completions_date ON daily_reminder_completions(completion_date);
CREATE INDEX idx_daily_reminder_completions_reminder_date ON daily_reminder_completions(reminder_id, completion_date);

-- ==================== 트리거 함수 생성 ====================
-- updated_at 자동 업데이트 함수
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- ==================== 트리거 설정 ====================
-- users 테이블 updated_at 트리거
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- categories 테이블 updated_at 트리거
CREATE TRIGGER update_categories_updated_at 
    BEFORE UPDATE ON categories 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- tasks 테이블 updated_at 트리거
CREATE TRIGGER update_tasks_updated_at 
    BEFORE UPDATE ON tasks 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- pomodoro_goals 테이블 updated_at 트리거
CREATE TRIGGER update_pomodoro_goals_updated_at 
    BEFORE UPDATE ON pomodoro_goals 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- concentration_goals 테이블 updated_at 트리거
CREATE TRIGGER update_concentration_goals_updated_at 
    BEFORE UPDATE ON concentration_goals 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- time_attack_goals 테이블 updated_at 트리거
CREATE TRIGGER update_time_attack_goals_updated_at 
    BEFORE UPDATE ON time_attack_goals 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- forgetting_prevention_reminders 테이블 updated_at 트리거
CREATE TRIGGER update_forgetting_prevention_reminders_updated_at 
    BEFORE UPDATE ON forgetting_prevention_reminders 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ==================== 기본 데이터 삽입 ====================
-- 기본 오분이 아이템들 (상점 아이템)
INSERT INTO obooni_items (name, price, image_url, item_type) VALUES
('기본 티셔츠', 0, '/images/items/basic_tshirt.png', 'CLOTHING'),
('기본 바지', 0, '/images/items/basic_pants.png', 'CLOTHING'),
('빨간 모자', 100, '/images/items/red_hat.png', 'ACCESSORY'),
('파란 안경', 150, '/images/items/blue_glasses.png', 'ACCESSORY'),
('멜빵바지', 200, '/images/items/suspender_pants.png', 'CLOTHING'),
('빨간 맨투맨', 250, '/images/items/red_sweatshirt.png', 'CLOTHING'),
('노란 신발', 180, '/images/items/yellow_shoes.png', 'ACCESSORY'),
('초록 가방', 220, '/images/items/green_bag.png', 'ACCESSORY');

-- ==================== 마이그레이션 완료 ====================
-- 총 15개 테이블 생성 완료:
-- 1. users (사용자)
-- 2. coin_transactions (코인 거래)
-- 3. categories (카테고리)
-- 4. tasks (일정)
-- 5. growth_albums (성장앨범)
-- 6. pomodoro_goals (포모도로 목표)
-- 7. pomodoro_sessions (포모도로 세션)
-- 8. concentration_goals (집중도 목표)
-- 9. obooni_items (오분이 아이템)
-- 10. user_items (사용자 아이템)
-- 11. time_attack_goals (타임어택 목표)
-- 12. time_attack_sessions (타임어택 세션)
-- 13. time_attack_steps (타임어택 단계)
-- 14. forgetting_prevention_reminders (망각방지 알림)
-- 15. daily_reminder_completions (일일 알림 완료)
