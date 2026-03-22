-- member.state와 불일치하는 서브 테이블 행 제거 (과거 버그·수동 조작 등으로 남은 고아 행 정리)

DELETE am
FROM active_member am
    INNER JOIN member m ON am.member_id = m.id
WHERE
    m.state <> 'ACTIVE';

DELETE im
FROM inactive_member im
    INNER JOIN member m ON im.member_id = m.id
WHERE
    m.state <> 'INACTIVE';

DELETE cm
FROM completed_member cm
    INNER JOIN member m ON cm.member_id = m.id
WHERE
    m.state <> 'COMPLETED';

DELETE gm
FROM graduated_member gm
    INNER JOIN member m ON gm.member_id = m.id
WHERE
    m.state <> 'GRADUATED';

DELETE wm
FROM withdrawn_member wm
    INNER JOIN member m ON wm.member_id = m.id
WHERE
    m.state <> 'WITHDRAWN';
