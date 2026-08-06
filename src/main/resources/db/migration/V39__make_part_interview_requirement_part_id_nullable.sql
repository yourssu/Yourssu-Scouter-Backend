ALTER TABLE part_interview_requirement
    DROP FOREIGN KEY fk_part_interview_requirement_part;

ALTER TABLE part_interview_requirement
    MODIFY COLUMN part_id BIGINT NULL;

ALTER TABLE part_interview_requirement
    ADD CONSTRAINT fk_part_interview_requirement_part
        FOREIGN KEY (part_id) REFERENCES part (id);
