-- 비액티브: 사유, 문자회신여부, 문자회신 희망시기 (파싱/입력은 추후, 응답에 포함)
ALTER TABLE inactive_member
    ADD COLUMN reason TEXT NULL,
    ADD COLUMN sms_replied BOOLEAN NULL,
    ADD COLUMN sms_reply_desired_period VARCHAR(100) NULL;

-- 탈퇴: 탈퇴 일자 별도 컬럼 (기존에는 note에만 포함)
ALTER TABLE withdrawn_member
    ADD COLUMN withdrawn_date DATE NULL;
