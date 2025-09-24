-- V4__fix_language_and_alarm_status.sql
ALTER TABLE users
    ADD COLUMN alarm_status BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN deactivated_at TIMESTAMP NULL;
