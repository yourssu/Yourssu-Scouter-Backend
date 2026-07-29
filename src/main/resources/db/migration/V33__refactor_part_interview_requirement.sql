DROP TABLE IF EXISTS part_interview_requirement;

CREATE TABLE part_interview_requirement (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_id     BIGINT NOT NULL,
    semester_id BIGINT NOT NULL,
    rubric_type VARCHAR(50) NOT NULL,
    content     TEXT NOT NULL,
    CONSTRAINT fk_part_interview_requirement_part
        FOREIGN KEY (part_id) REFERENCES part (id),
    CONSTRAINT fk_part_interview_requirement_semester
        FOREIGN KEY (semester_id) REFERENCES semester (id)
);
