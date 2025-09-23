-- V3__add_language_and_alarm_status.sql

ALTER TABLE users
ADD COLUMN language VARCHAR(50);
ADD COLUMN alarm_status BOOLEAN NOT NULL DEFAULT TRUE;