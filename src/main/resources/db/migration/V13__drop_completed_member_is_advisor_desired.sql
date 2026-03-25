-- 수료(completed)에서는 Advisor 희망을 사용하지 않음. API·도메인에서 제거에 맞춰 컬럼 삭제
ALTER TABLE completed_member
    DROP COLUMN is_advisor_desired;
