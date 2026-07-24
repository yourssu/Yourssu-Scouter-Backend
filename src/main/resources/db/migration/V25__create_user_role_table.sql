CREATE TABLE user_role (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT      NOT NULL,
    role    VARCHAR(50) NOT NULL,
    CONSTRAINT uk_user_role_user_id_role
        UNIQUE (user_id, role)
);

INSERT INTO user_role (user_id, role)
SELECT id, 'SCOUTER_ADMIN'
FROM users
WHERE email IN (
    'umi.urssu@gmail.com',
    'feca.urssu@gmail.com',
    'nari.urssu@gmail.com',
    'emin.urssu@gmail.com',
    'piki.urssu@gmail.com',
    'logan.urssu@gmail.com',
    'ori.urssu@gmail.com',
    'enji.urssu@gmail.com',
    'jerome.urssu@gmail.com',
    'juun.urssu@gmail.com'
);
