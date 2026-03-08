-- V5에서 추가한 is_on_leave_of_absence 컬럼명을 is_on_leave 로 변경
ALTER TABLE active_member
    CHANGE COLUMN is_on_leave_of_absence is_on_leave BOOLEAN NULL;
