package com.yourssu.scouter.common.implement.domain.mail.reservation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

@Suppress("NonAsciiCharacters")
class MailReservationTest {
    private val now = Instant.parse("2026-05-05T00:00:00Z")

    private fun reservation(
        reservationTime: Instant = now.plusSeconds(60),
        status: MailReservationStatus = MailReservationStatus.SCHEDULED,
    ): MailReservation {
        return MailReservation(
            id = 1L,
            mailId = 10L,
            groupId = 100L,
            reservationTime = reservationTime,
            status = status,
            claimedAt = Instant.parse("2026-05-04T23:59:00Z"),
        )
    }

    @Test
    fun `canRetry는 예약 시간이 지났고 SENT가 아니면 true를 반환한다`() {
        val reservation =
            reservation(
                reservationTime = now.minusSeconds(1),
                status = MailReservationStatus.PENDING_SEND,
            )

        val result = reservation.canRetry(now)

        assertThat(result).isTrue()
    }

    @Test
    fun `canRetry는 SENT 상태이면 false를 반환한다`() {
        val reservation =
            reservation(
                reservationTime = now.minusSeconds(1),
                status = MailReservationStatus.SENT,
            )

        val result = reservation.canRetry(now)

        assertThat(result).isFalse()
    }

    @Test
    fun `canRetry는 예약 시간이 미래이면 false를 반환한다`() {
        val reservation =
            reservation(
                reservationTime = now.plusSeconds(1),
                status = MailReservationStatus.PENDING_SEND,
            )

        val result = reservation.canRetry(now)

        assertThat(result).isFalse()
    }

    @Test
    fun `canCancel은 예약 시간이 미래이고 SENT나 SENDING이 아니면 true를 반환한다`() {
        val reservation =
            reservation(
                reservationTime = now.plusSeconds(1),
                status = MailReservationStatus.SCHEDULED,
            )

        val result = reservation.canCancel(now)

        assertThat(result).isTrue()
    }

    @Test
    fun `canCancel은 SENT 상태이면 false를 반환한다`() {
        val reservation =
            reservation(
                reservationTime = now.plusSeconds(1),
                status = MailReservationStatus.SENT,
            )

        val result = reservation.canCancel(now)

        assertThat(result).isFalse()
    }

    @Test
    fun `canCancel은 SENDING 상태이면 false를 반환한다`() {
        val reservation =
            reservation(
                reservationTime = now.plusSeconds(1),
                status = MailReservationStatus.SENDING,
            )

        val result = reservation.canCancel(now)

        assertThat(result).isFalse()
    }

    @Test
    fun `canCancel은 예약 시간이 과거이면 false를 반환한다`() {
        val reservation =
            reservation(
                reservationTime = now.minusSeconds(1),
                status = MailReservationStatus.SCHEDULED,
            )

        val result = reservation.canCancel(now)

        assertThat(result).isFalse()
    }

    @Test
    fun `canEdit은 예약 시간이 미래이고 SENT나 SENDING이 아니면 true를 반환한다`() {
        val reservation =
            reservation(
                reservationTime = now.plusSeconds(1),
                status = MailReservationStatus.PENDING_SEND,
            )

        val result = reservation.canEdit(now)

        assertThat(result).isTrue()
    }

    @Test
    fun `canEdit은 SENT 상태이면 false를 반환한다`() {
        val reservation =
            reservation(
                reservationTime = now.plusSeconds(1),
                status = MailReservationStatus.SENT,
            )

        val result = reservation.canEdit(now)

        assertThat(result).isFalse()
    }

    @Test
    fun `canEdit은 SENDING 상태이면 false를 반환한다`() {
        val reservation =
            reservation(
                reservationTime = now.plusSeconds(1),
                status = MailReservationStatus.SENDING,
            )

        val result = reservation.canEdit(now)

        assertThat(result).isFalse()
    }

    @Test
    fun `canEdit은 예약 시간이 과거이면 false를 반환한다`() {
        val reservation =
            reservation(
                reservationTime = now.minusSeconds(1),
                status = MailReservationStatus.SCHEDULED,
            )

        val result = reservation.canEdit(now)

        assertThat(result).isFalse()
    }

    @Test
    fun `markSent는 SENT 상태의 복사본을 반환하고 나머지 필드는 유지한다`() {
        val reservation = reservation(status = MailReservationStatus.SENDING)

        val result = reservation.markSent()

        assertThat(result.status).isEqualTo(MailReservationStatus.SENT)
        assertThat(result.id).isEqualTo(reservation.id)
        assertThat(result.mailId).isEqualTo(reservation.mailId)
        assertThat(result.groupId).isEqualTo(reservation.groupId)
        assertThat(result.reservationTime).isEqualTo(reservation.reservationTime)
        assertThat(result.claimedAt).isEqualTo(reservation.claimedAt)
    }

    @Test
    fun `markPendingSend는 PENDING_SEND 상태의 복사본을 반환하고 나머지 필드는 유지한다`() {
        val reservation = reservation(status = MailReservationStatus.SENDING)

        val result = reservation.markPendingSend()

        assertThat(result.status).isEqualTo(MailReservationStatus.PENDING_SEND)
        assertThat(result.id).isEqualTo(reservation.id)
        assertThat(result.mailId).isEqualTo(reservation.mailId)
        assertThat(result.groupId).isEqualTo(reservation.groupId)
        assertThat(result.reservationTime).isEqualTo(reservation.reservationTime)
        assertThat(result.claimedAt).isEqualTo(reservation.claimedAt)
    }
}
