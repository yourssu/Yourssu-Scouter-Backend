#!/usr/bin/env bash
# 로컬 코드로 실행하되 dev DB 사용 (application-local + application-local-dev-db)
# .env.local 에 DB_URL, DB_USERNAME, DB_PASSWORD (dev DB 접속 정보) 설정 필요.
# 운영(prod) DB에 연결하지 마세요. 예약 메일 스케줄러는 local 프로필에서 기본 OFF입니다.
# 사용법: ./scripts/run-local-with-dev-db.sh

set -e
cd "$(dirname "$0")/.."

if [ ! -f .env.local ]; then
  echo ".env.local 파일이 없습니다. DB_URL, DB_USERNAME, DB_PASSWORD 를 넣어 두세요."
  exit 1
fi

set -a
source .env.local
set +a

for var in DB_URL DB_USERNAME DB_PASSWORD; do
  if [ -z "${!var}" ]; then
    echo "환경변수 $var 이 비어 있습니다. .env.local 에 dev DB 접속 정보를 넣어 주세요."
    exit 1
  fi
done

./gradlew bootRun --args='--spring.profiles.active=local,local-dev-db' --no-daemon
