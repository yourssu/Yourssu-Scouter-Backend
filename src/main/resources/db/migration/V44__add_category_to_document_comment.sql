ALTER TABLE document_comment
    ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'DOCUMENT';

ALTER TABLE document_comment
    ALTER COLUMN category DROP DEFAULT;

DROP TABLE IF EXISTS interview_memo;
