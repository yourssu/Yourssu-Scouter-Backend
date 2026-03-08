#!/usr/bin/env bash
# 로컬 실행 시 .env.local 로드 (dev와 동일한 JWT 시크릿 등 사용 — dev에서 발급한 토큰으로 로컬 API 호출 가능)
# 사용법: ./scripts/run-local-with-env-local.sh

set -e
cd "$(dirname "$0")/.."

if [ ! -f .env.local ]; then
  echo ".env.local 파일이 없습니다. dev와 동일한 시크릿을 쓰려면 .env.local을 만들어 두세요."
  exit 1
fi

set -a
source .env.local
set +a

./gradlew bootRun --args='--spring.profiles.active=local' --no-daemon
