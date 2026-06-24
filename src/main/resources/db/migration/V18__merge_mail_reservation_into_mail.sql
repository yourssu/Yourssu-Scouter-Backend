ALTER TABLE mail
    ADD COLUMN group_id         BIGINT       NULL,
    ADD COLUMN reservation_time DATETIME(6)  NOT NULL DEFAULT '1970-01-01 00:00:00.000000',
    ADD COLUMN status           VARCHAR(20)  NOT NULL DEFAULT 'SENT',
    ADD COLUMN claimed_at       DATETIME(6)  NULL;

UPDATE mail m
    JOIN mail_reservation r ON r.mail_id = m.id
SET m.group_id         = r.group_id,
    m.reservation_time = r.reservation_time,
    m.status           = r.status,
    m.claimed_at       = r.claimed_at;

DROP TABLE mail_reservation;
