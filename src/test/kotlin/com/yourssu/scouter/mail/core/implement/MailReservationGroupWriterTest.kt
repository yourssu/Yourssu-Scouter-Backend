package com.yourssu.scouter.mail.core.implement

import com.yourssu.scouter.mail.core.business.MailBodyFormat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

@Suppress("NonAsciiCharacters")
class MailReservationGroupWriterTest {
    private val groupRepository = mock<MailReservationGroupRepository>()
    private val reservationRepository = mock<MailReservationRepository>()
    private val writer = MailReservationGroupWriter(groupRepository, reservationRepository)

    private fun group(status: MailReservationStatus) =
        MailReservationGroup(
            id = 1L,
            reservedByUserId = 1L,
            templateId = 1L,
            reservationTime = Instant.parse("2026-03-01T00:00:00Z"),
            status = status,
        )

    private fun reservation(
        id: Long,
        status: MailReservationStatus,
    ) = MailReservation(
        id = id,
        reservedByUserId = 1L,
        receiverEmailAddress = "to$id@example.com",
        mailSubject = "제목",
        mailBody = "본문",
        bodyFormat = MailBodyFormat.HTML,
        groupId = 1L,
        reservationTime = Instant.parse("2026-03-01T00:00:00Z"),
        status = status,
    )

    @Test
    fun `syncStatus는 소속 메일이 전부 발송되면 그룹을 SENT로 갱신한다`() {
        whenever(groupRepository.findById(1L)).thenReturn(group(MailReservationStatus.SCHEDULED))
        whenever(reservationRepository.findAllByGroupId(1L)).thenReturn(
            listOf(reservation(10L, MailReservationStatus.SENT), reservation(11L, MailReservationStatus.SENT)),
        )

        writer.syncStatus(1L)

        verify(groupRepository).updateStatus(1L, MailReservationStatus.SENT)
    }

    @Test
    fun `syncStatus는 계산된 상태가 현재와 같으면 갱신하지 않는다`() {
        whenever(groupRepository.findById(1L)).thenReturn(group(MailReservationStatus.SCHEDULED))
        whenever(reservationRepository.findAllByGroupId(1L)).thenReturn(
            listOf(reservation(10L, MailReservationStatus.SCHEDULED)),
        )

        writer.syncStatus(1L)

        verify(groupRepository, never()).updateStatus(any(), any())
    }

    @Test
    fun `syncStatus는 소속 메일이 없으면 상태를 변경하지 않는다`() {
        whenever(groupRepository.findById(1L)).thenReturn(group(MailReservationStatus.SENDING))
        whenever(reservationRepository.findAllByGroupId(1L)).thenReturn(emptyList())

        writer.syncStatus(1L)

        verify(groupRepository, never()).updateStatus(any(), any())
    }

    @Test
    fun `syncStatusOfGroupsIn은 해당 상태의 그룹들을 재계산한다`() {
        whenever(groupRepository.findAllByStatus(MailReservationStatus.SENDING)).thenReturn(
            listOf(group(MailReservationStatus.SENDING)),
        )
        whenever(groupRepository.findById(1L)).thenReturn(group(MailReservationStatus.SENDING))
        whenever(reservationRepository.findAllByGroupId(1L)).thenReturn(
            listOf(reservation(10L, MailReservationStatus.PENDING_SEND)),
        )

        writer.syncStatusOfGroupsIn(MailReservationStatus.SENDING)

        verify(groupRepository).updateStatus(1L, MailReservationStatus.PENDING_SEND)
    }
}
