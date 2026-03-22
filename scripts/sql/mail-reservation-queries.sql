SELECT
    r.id AS reservation_id,
    r.status AS reservation_status,
    r.reservation_time AS reservation_time,
    m.id AS mail_id,
    m.sender_email_address,
    m.mail_subject,
    m.mail_body,
    m.body_format,
    GROUP_CONCAT(
        CASE
            WHEN ra.type = 'TO' THEN ra.email_address
        END
        ORDER BY ra.id SEPARATOR ','
    ) AS to_addresses,
    GROUP_CONCAT(
        CASE
            WHEN ra.type = 'CC' THEN ra.email_address
        END
        ORDER BY ra.id SEPARATOR ','
    ) AS cc_addresses,
    GROUP_CONCAT(
        CASE
            WHEN ra.type = 'BCC' THEN ra.email_address
        END
        ORDER BY ra.id SEPARATOR ','
    ) AS bcc_addresses
FROM
    mail_reservation r
    JOIN mail m ON r.mail_id = m.id
    LEFT JOIN mail_recipient_address ra ON ra.mail_id = m.id
WHERE
    m.sender_email_address = 'glen.urssu@gmail.com'
    -- 예약(미발송)만 보고 싶으면 아래 주석 해제
    AND r.status IN ('SCHEDULED', 'PENDING_SEND')
GROUP BY
    r.id,
    r.status,
    r.reservation_time,
    m.id,
    m.sender_email_address,
    m.mail_subject,
    m.mail_body,
    m.body_format
ORDER BY r.reservation_time;\G


SELECT
    r.id AS reservation_id,
    r.status AS reservation_status,
    r.reservation_time AS reservation_time,
    m.id AS mail_id,
    m.sender_email_address,
    m.mail_subject,
    m.mail_body,
    m.body_format,
    ra.type AS recipient_type, -- TO / CC / BCC
    ra.email_address AS recipient_address
FROM
    mail_reservation r
    JOIN mail m ON r.mail_id = m.id
    LEFT JOIN mail_recipient_address ra ON ra.mail_id = m.id
WHERE
    m.sender_email_address = 'glen.urssu@gmail.com'
    AND r.status IN ('SCHEDULED', 'PENDING_SEND')  -- 필요 시 예약건만
ORDER BY r.reservation_time, r.id, ra.type, ra.id;
