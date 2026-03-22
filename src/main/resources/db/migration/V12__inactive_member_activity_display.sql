ALTER TABLE inactive_member
    ADD COLUMN activity_semesters_label TEXT NULL;

ALTER TABLE inactive_member
    ADD COLUMN total_active_semesters INT NULL;
