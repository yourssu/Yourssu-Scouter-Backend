#!/usr/bin/env bash
# .env.local 의 DB_URL, DB_USERNAME, DB_PASSWORD 를 로드한 뒤 Gradle Flyway 태스크 실행.
# 사용 예:
#   ./scripts/flyway-with-dev-db-env.sh flywayRepair
#   ./scripts/flyway-with-dev-db-env.sh flywayMigrate
#   ./scripts/flyway-with-dev-db-env.sh flywayInfo

set -e
cd "$(dirname "$0")/.."

if [ ! -f .env.local ]; then
  echo ".env.local 파일이 없습니다. DB_URL, DB_USERNAME, DB_PASSWORD 를 설정하세요."
  exit 1
fi

set -a
source .env.local
set +a

for var in DB_URL DB_USERNAME DB_PASSWORD; do
  if [ -z "${!var}" ]; then
    echo "환경변수 $var 이 비어 있습니다. .env.local 을 확인하세요."
    exit 1
  fi
done

exec ./gradlew "$@" --no-daemon
