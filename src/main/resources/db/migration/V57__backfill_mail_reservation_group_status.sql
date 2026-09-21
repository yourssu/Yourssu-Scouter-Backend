-- 그룹 상태가 생성 이후 갱신되지 않던 문제(#498) 백필: 소속 메일 상태로부터 그룹 상태를 재계산한다.
-- 규칙은 MailReservationGroup.resolveStatus 와 동일하다. 소속 메일이 없는 그룹은 삭제한다.
UPDATE mail_reservation_group g
    JOIN (SELECT group_id,
                 COUNT(*)                     AS total,
                 SUM(status = 'SENDING')      AS sending,
                 SUM(status = 'SENT')         AS sent,
                 SUM(status = 'PENDING_SEND') AS pending
          FROM mail
          WHERE group_id IS NOT NULL
          GROUP BY group_id) m ON m.group_id = g.id
SET g.status = CASE
                   WHEN m.sending > 0 THEN 'SENDING'
                   WHEN m.sent = m.total THEN 'SENT'
                   WHEN m.pending > 0 THEN 'PENDING_SEND'
                   ELSE 'SCHEDULED'
    END;

DELETE g
FROM mail_reservation_group g
WHERE NOT EXISTS (SELECT 1 FROM mail m WHERE m.group_id = g.id);
