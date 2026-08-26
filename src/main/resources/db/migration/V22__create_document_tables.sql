ALTER TABLE applicant_answer
    ADD COLUMN section_id BIGINT NULL;

CREATE TABLE document_section (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_id          BIGINT NOT NULL,
    question         TEXT   NOT NULL,
    max_score        INT    NOT NULL,
    criterion_detail VARCHAR(255) NOT NULL,
    CONSTRAINT fk_document_section_part
        FOREIGN KEY (part_id) REFERENCES part (id)
);

CREATE TABLE document_evaluation (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    applicant_id       BIGINT NOT NULL,
    evaluator_user_id  BIGINT NOT NULL,
    overall_comment    TEXT   NOT NULL,
    result             VARCHAR(20) NOT NULL,
    submitted_at       DATETIME NULL,
    CONSTRAINT uk_document_evaluation_applicant_evaluator
        UNIQUE (applicant_id, evaluator_user_id)
);

CREATE TABLE document_evaluation_item (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    evaluation_id  BIGINT NOT NULL,
    section_id     BIGINT NOT NULL,
    score          INT    NOT NULL,
    memo           TEXT   NOT NULL,
    CONSTRAINT fk_document_evaluation_item_evaluation
        FOREIGN KEY (evaluation_id) REFERENCES document_evaluation (id) ON DELETE CASCADE
);

CREATE TABLE document_comment (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    applicant_id     BIGINT NOT NULL,
    section_id       BIGINT NOT NULL,
    author_user_id   BIGINT NOT NULL,
    content          TEXT   NOT NULL,
    created_at       DATETIME NOT NULL
);

CREATE TABLE part_document_deadline (
    part_id  BIGINT PRIMARY KEY,
    deadline DATETIME NOT NULL
);
