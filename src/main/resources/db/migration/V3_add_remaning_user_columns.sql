ALTER TABLE users
ADD COLUMN last_reminder_coin_date DATE,
ADD COLUMN last_login DATE,
ADD COLUMN last_task_coin_date DATE,
ADD COLUMN fcm_token VARCHAR(255);