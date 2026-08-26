ALTER TABLE assigned_question
    DROP FOREIGN KEY fk_assigned_question_assigned_member;

ALTER TABLE assigned_question
    MODIFY COLUMN assigned_member_id BIGINT NULL;

ALTER TABLE assigned_question
    ADD CONSTRAINT fk_assigned_question_assigned_member
        FOREIGN KEY (assigned_member_id) REFERENCES member (id);
