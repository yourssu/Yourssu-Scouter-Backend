-- 구글 프로필 이미지 URL 길이가 기존 컬럼(VARCHAR(255) 기본값)을 넘는 계정이 있어,
-- 신규 유저 첫 로그인 시 INSERT가 'Data too long for column profile_image_url'로 실패했다.
-- 구글이 이 URL 형식의 최대 길이를 보장하지 않으므로, VARCHAR 길이를 추측하는 대신 TEXT로 바꾼다.
ALTER TABLE users
    MODIFY COLUMN profile_image_url TEXT NOT NULL;
