CREATE TABLE mail_reservation_group (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_email     VARCHAR(255) NOT NULL,
    template_id      BIGINT       NULL,
    reservation_time DATETIME(6)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    created_at       DATETIME(6)  NOT NULL
);

ALTER TABLE mail_reservation
    ADD COLUMN group_id BIGINT NULL,
    ADD CONSTRAINT fk_mail_reservation_group
        FOREIGN KEY (group_id) REFERENCES mail_reservation_group (id);
