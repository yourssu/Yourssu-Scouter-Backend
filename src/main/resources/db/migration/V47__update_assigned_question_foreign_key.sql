-- V47__update_assigned_question_foreign_key.sql

ALTER TABLE assigned_question
    DROP FOREIGN KEY fk_assigned_question_assigned_interviewer;

ALTER TABLE assigned_question
    ADD CONSTRAINT fk_assigned_question_assigned_member
        FOREIGN KEY (assigned_member_id) REFERENCES member (id);