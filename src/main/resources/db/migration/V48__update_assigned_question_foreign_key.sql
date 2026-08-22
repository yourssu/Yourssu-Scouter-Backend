ALTER TABLE assigned_question
    ADD CONSTRAINT fk_assigned_question_assigned_member
        FOREIGN KEY (assigned_member_id) REFERENCES member (id);