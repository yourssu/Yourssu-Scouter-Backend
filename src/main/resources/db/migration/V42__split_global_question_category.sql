UPDATE question
SET category = CASE
    WHEN category = 'GLOBAL' AND content = '마지막으로 하고 싶은 말이 있나요?' THEN 'OUTRO'
    WHEN category = 'GLOBAL' THEN 'INTRO'
    ELSE category
END;

UPDATE assigned_question aq
LEFT JOIN question q ON aq.source_question_id = q.id
SET aq.category = CASE
    WHEN aq.category = 'GLOBAL' AND q.category IN ('INTRO', 'OUTRO') THEN q.category
    WHEN aq.category = 'GLOBAL' THEN 'INTRO'
    ELSE aq.category
END;
