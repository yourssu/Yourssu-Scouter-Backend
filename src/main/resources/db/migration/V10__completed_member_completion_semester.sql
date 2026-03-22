-- 수료 멤버: 활동 기간(시작·끝 학기) 제거, 수료 학기 단일 FK로 통일
ALTER TABLE completed_member
    ADD COLUMN completion_semester_id BIGINT NULL AFTER member_id;

UPDATE completed_member
SET completion_semester_id = active_end_semester_id
WHERE completion_semester_id IS NULL;

ALTER TABLE completed_member
    MODIFY COLUMN completion_semester_id BIGINT NOT NULL;

ALTER TABLE completed_member
    ADD CONSTRAINT fk_completed_member_completion_semester
        FOREIGN KEY (completion_semester_id) REFERENCES semester (id);

ALTER TABLE completed_member
    DROP FOREIGN KEY fk_completed_member_active_start_semester;

ALTER TABLE completed_member
    DROP FOREIGN KEY fk_completed_member_active_end_semester;

ALTER TABLE completed_member
    DROP COLUMN active_start_semester_id,
    DROP COLUMN active_end_semester_id;
