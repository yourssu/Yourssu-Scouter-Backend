package com.yourssu.scouter.common.implement.domain.mail.reservation

import java.time.Instant
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class MailReservationReader(
    private val mailReservationRepository: MailReservationRepository,
) {

    fun readAll(): List<MailReservation> {
        return mailReservationRepository.findAll()
    }

    fun readAllBefore(time: Instant): List<MailReservation> {
        return mailReservationRepository.findAllByReservationTimeLessThanEqual(time)
    }

    /** 발송 대기 예약만 조회 (스케줄러가 claim 대상으로 삼는 상태만; SENDING·SENT 제외) */
    fun readAllPendingBefore(time: Instant): List<MailReservation> {
        return mailReservationRepository.findAllByReservationTimeLessThanEqualAndStatusIn(
            time,
            listOf(MailReservationStatus.SCHEDULED, MailReservationStatus.PENDING_SEND),
        )
    }

    fun readAllBeforeByReservedByUserIds(time: Instant, reservedByUserIds: Collection<Long>): List<MailReservation> {
        return mailReservationRepository.findAllByReservationTimeLessThanEqualAndReservedByUserIds(time, reservedByUserIds)
    }

    fun readAllByReservedByUserIds(reservedByUserIds: Collection<Long>): List<MailReservation> {
        return mailReservationRepository.findAllByReservedByUserIds(reservedByUserIds)
    }

    fun readById(id: Long): MailReservation? {
        return mailReservationRepository.findById(id)
    }

    fun readAllByGroupId(groupId: Long): List<MailReservation> {
        return mailReservationRepository.findAllByGroupId(groupId)
    }
}
