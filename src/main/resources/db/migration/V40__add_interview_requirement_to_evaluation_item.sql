ALTER TABLE interview_evaluation_item
    ADD COLUMN interview_requirement_id BIGINT NULL,
    ADD CONSTRAINT fk_interview_rubric_item_requirement
        FOREIGN KEY (interview_requirement_id)
        REFERENCES part_interview_requirement (id)
        ON DELETE SET NULL;
