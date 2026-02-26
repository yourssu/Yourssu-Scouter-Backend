#!/usr/bin/env bash
# 로컬 메일 예약 API 호출 예시 (로그 확인용)
# 사용법: ACCESS_TOKEN="발급받은_JWT" ./scripts/local-mail-reservation-curl.sh
# - 토큰: 로그인(Google OAuth) 후 받은 accessToken
# - 예약 시각: 과거로 넣으면 다음 스케줄러 주기(1분) 안에 발송 대상으로 처리됨

set -e
BASE_URL="${BASE_URL:-http://localhost:8080}"
if [ -z "${ACCESS_TOKEN}" ]; then
  echo "ACCESS_TOKEN 환경변수를 설정하세요. (예: export ACCESS_TOKEN=\"Bearer eyJ...\")"
  exit 1
fi

# reservationTime: 과거로 넣으면 곧바로 발송 대상이 됨 (UTC). 현재 기준 1분 전 권장.
# KST 16시 = UTC 07시 → "2026-02-20T07:00:00Z"
# "곧 테스트"하려면 여기를 현재 UTC 시각 - 1분 정도로 변경
RESERVATION_TIME="${RESERVATION_TIME:-2026-02-20T07:14:09.425Z}"

curl -s -X POST "${BASE_URL}/api/mails/reservation" \
  -H "Content-Type: application/json" \
  -H "Authorization: ${ACCESS_TOKEN}" \
  -d "{
    \"receiverEmailAddresses\": [\"nanseulgim1027@gmail.com\"],
    \"ccEmailAddresses\": [],
    \"bccEmailAddresses\": [\"emin.urssu@gmail.com\", \"piki.urssu@gmail.com\", \"nari.urssu@gmail.com\", \"feca.urssu@gmail.com\"],
    \"mailSubject\": \"스카우터 메일테스트...몇번째인지 모르겠는데 일단 kst기준 2/20 16시에 와야함\",
    \"mailBody\": \"<p>안녕하세요 2/20 16시님</p>\",
    \"bodyFormat\": \"HTML\",
    \"reservationTime\": \"${RESERVATION_TIME}\"
  }"

echo ""
echo "예약 요청 완료. 로그에서 다음 순서로 확인하세요:"
echo "  1. 메일 예약 등록 요청 / 메일 예약 저장 완료"
echo "  2. 1분마다: 예약 메일 스케줄러 실행 → 예약 메일 처리 시작 → 발송대상건수 → 예약 처리 시작 → 예약 메일 발송 완료"
