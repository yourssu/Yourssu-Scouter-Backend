ALTER TABLE member
    ADD COLUMN user_id BIGINT NULL,
    ADD CONSTRAINT fk_user_id
    FOREIGN KEY (user_id)
        REFERENCES USERS (id) on DELETE SET NULL;
