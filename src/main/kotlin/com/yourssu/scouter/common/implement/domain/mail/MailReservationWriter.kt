package com.yourssu.scouter.common.implement.domain.mail

import java.time.Instant
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MailReservationWriter(
    private val mailReservationRepository: MailReservationRepository,
) {
    fun delete(mailReservation: MailReservation) {
        mailReservationRepository.deleteById(mailReservation.id!!)
    }

    /**
     * 단일 행에 대해 발송 claim. 성공 시 SENDING으로 갱신된 뒤 최신 도메인을 반환한다.
     *
     * @param now 예약 발송 시각 판단 기준(SQL: `reservation_time <= now`). 스케줄러·재전송이 “지금 시각”을 넘길 때 사용.
     * DB `claimed_at`에는 **이 메서드가 실행되는 시점**의 [Instant.now]가 별도로 기록되며, [now]와 같을 필요는 없다(고착 복구·지연 발송 판별용).
     */
    fun claimForSendingOrNull(
        id: Long,
        now: Instant,
    ): MailReservation? {
        val claimedAt = Instant.now()
        if (mailReservationRepository.tryClaimForSending(id, claimedAt, now) != 1) {
            return null
        }
        return mailReservationRepository.findById(id)
    }

    fun markAsSent(reservation: MailReservation) {
        mailReservationRepository.save(
            reservation.copy(status = MailReservationStatus.SENT, claimedAt = null),
        )
    }

    fun markAsPendingSend(reservation: MailReservation) {
        mailReservationRepository.save(
            reservation.copy(status = MailReservationStatus.PENDING_SEND, claimedAt = null),
        )
    }

    fun resetStuckSendingReservations(claimedBefore: Instant): Int {
        return mailReservationRepository.resetStuckSendingReservations(claimedBefore)
    }
}
