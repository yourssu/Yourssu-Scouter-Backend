package com.yourssu.scouter.common.implement.domain.mail

enum class MailReservationStatus {
    /** 예약됨 - 발송 시각 전 */
    SCHEDULED,

    /** 예약 시간 지났는데 아직 발송 안 됨 (오류/재시도 대기) */
    PENDING_SEND,

    /** 발송 워커가 단일 승자로 점유한 상태 (Gmail 전송 직전~완료/실패 처리) */
    SENDING,

    /** 예약해서 이미 발송 완료 */
    SENT,
}
