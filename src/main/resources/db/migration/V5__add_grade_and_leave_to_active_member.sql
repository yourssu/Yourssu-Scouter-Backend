-- 액티브 멤버 전용: 학년·재휴학여부 (엑셀 파싱 없음, API 조회·PATCH로만 사용)
ALTER TABLE active_member
    ADD COLUMN grade INT NULL,
    ADD COLUMN is_on_leave_of_absence BOOLEAN NULL;
