#!/usr/bin/env bash
# Flyway 마이그레이션만 실행 (Spring Boot 앱 기동 없음)
# .env.local 에 DB_URL, DB_USERNAME, DB_PASSWORD 설정 필요.
# 사용법: ./scripts/run-flyway-migrate.sh

set -e
cd "$(dirname "$0")/.."

if [ -f .env.local ]; then
  set -a
  source .env.local
  set +a
fi

for var in DB_URL DB_USERNAME DB_PASSWORD; do
  if [ -z "${!var}" ]; then
    echo "환경변수 $var 이 비어 있습니다. .env.local 에 설정하거나 export 해 주세요."
    exit 1
  fi
done

echo "Flyway 마이그레이션 실행..."
./gradlew flywayMigrate --no-daemon
