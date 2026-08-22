ALTER TABLE assigned_question
    DROP FOREIGN KEY fk_assigned_question_assigned_interviewer;

ALTER TABLE assigned_question
    RENAME COLUMN assigned_interviewer_user_id TO assigned_member_id;

ALTER TABLE assigned_question
    ADD CONSTRAINT fk_assigned_question_assigned_member
        FOREIGN KEY (assigned_member_id) REFERENCES member (id);
