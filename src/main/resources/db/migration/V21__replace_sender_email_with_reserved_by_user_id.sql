-- 실제 발송은 공용 계정(mail.sender)으로 나가므로 sender_email 계열 컬럼은 발신 주소가 아니라
-- "누가 예약했는지" 메타정보였음. 예약자 식별자(users.id)로 교체하고 이름/이메일은 조회 시점에 파생한다.
ALTER TABLE mail_reservation_group
    ADD COLUMN reserved_by_user_id BIGINT NULL;

UPDATE mail_reservation_group g
    JOIN users u ON u.email = g.sender_email
SET g.reserved_by_user_id = u.id;

ALTER TABLE mail_reservation_group
    DROP COLUMN sender_email;

ALTER TABLE mail
    ADD COLUMN reserved_by_user_id BIGINT NULL;

UPDATE mail m
    JOIN users u ON u.email = m.sender_email_address
SET m.reserved_by_user_id = u.id;

ALTER TABLE mail
    DROP COLUMN sender_email_address;
