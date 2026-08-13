# Yourssu-Scouter-Backend

## 아키텍처

```
com.yourssu.scouter
├── auth/                  // 인증/인가 — 로그인, 토큰, 계정, 권한
│   ├── authentication/        // OAuth2 로그인 처리, 토큰 발급/갱신
│   ├── authorization/         // 사용자 권한(Role) 관리
│   ├── login/                 // OAuth2 로그인 + 회원 조회를 묶은 로그인 유스케이스
│   └── user/                  // 로그인 계정(User) 도메인
│
├── member/                // Yourssu 동아리 회원 관리
│   ├── core/                  // 회원 CRUD, 5단계 상태 관리(Active~Withdrawn)
│   ├── excel/                 // 엑셀로 회원 정보 일괄 가져오기/내보내기
│   └── applicantsync/         // recruiting 최종합격자를 자동으로 회원으로 등록
│
├── masterdata/            // 자주 안 바뀌는 기준 데이터
│   ├── college/               // 단과대
│   ├── department/            // 학과
│   ├── division/              // Yourssu 사업부 
│   ├── part/                  // Yourssu 파트 
│   └── semester/              // 학기
│
├── mail/                  // 메일 발송 (작업 진행 중)
│
├── recruiting/            // 채용/지원자 관리 (작업 진행 중)
│
├── admin/                 // 관리자 (작업 진행 중)
|
└── common/                // 앱 전역 인프라 — 설정, 예외 베이스 등
```

※ 하위 도메인이 `core`면 해당 상위 도메인의 본체(가장 핵심적인 기본 기능)를 의미합니다. (예: `member/core`)
