-- Step 1: 컬럼 크기 먼저 확장
ALTER TABLE forgetting_prevention_reminders
ALTER COLUMN repetition_days TYPE VARCHAR(50);

-- Step 2: 그 다음 데이터 변환
UPDATE forgetting_prevention_reminders
SET repetition_days = TRIM(BOTH ',' FROM
    CONCAT(
        CASE WHEN substring(repetition_days,1,1) <> '-' THEN 'MON,' ELSE '' END,
        CASE WHEN substring(repetition_days,2,1) <> '-' THEN 'TUE,' ELSE '' END,
        CASE WHEN substring(repetition_days,3,1) <> '-' THEN 'WED,' ELSE '' END,
        CASE WHEN substring(repetition_days,4,1) <> '-' THEN 'THU,' ELSE '' END,
        CASE WHEN substring(repetition_days,5,1) <> '-' THEN 'FRI,' ELSE '' END,
        CASE WHEN substring(repetition_days,6,1) <> '-' THEN 'SAT,' ELSE '' END,
        CASE WHEN substring(repetition_days,7,1) <> '-' THEN 'SUN,' ELSE '' END
    )
);