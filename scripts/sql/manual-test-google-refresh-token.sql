-- 수동 테스트: GET /oauth2/google-refresh-token-status (invalid_grant 재현)
-- 사용법: 1) 로그인 후 이메일로 유저 조회 → 2) refresh_token을 'abc'로 변경 → 3) API 호출 후 401 확인 → 4) 아래 복구 쿼리 또는 재로그인

-- 1. 로그인한 유저 확인 (이메일로 조회)
SELECT id, email, refresh_token
FROM users
WHERE email = '본인이메일@example.com';

-- 2. refresh_token을 잘못된 값으로 변경 (위에서 확인한 id 사용)
-- UPDATE users SET refresh_token = 'abc' WHERE id = <USER_ID>;

-- 3. (테스트 후) 원래 refresh_token으로 복구할 때 사용
-- UPDATE users SET refresh_token = '원래_복사해둔_값' WHERE id = <USER_ID>;
