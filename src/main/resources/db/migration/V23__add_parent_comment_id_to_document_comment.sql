ALTER TABLE document_comment
    ADD COLUMN parent_comment_id BIGINT NULL,
    ADD CONSTRAINT fk_document_comment_parent
        FOREIGN KEY (parent_comment_id) REFERENCES document_comment (id) ON DELETE CASCADE;
