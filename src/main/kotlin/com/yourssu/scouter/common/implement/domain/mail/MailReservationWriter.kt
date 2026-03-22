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

    fun resetStuckSendingReservations(claimedBefore: Instant): Int {
        return mailReservationRepository.resetStuckSendingReservations(claimedBefore)
    }
}
