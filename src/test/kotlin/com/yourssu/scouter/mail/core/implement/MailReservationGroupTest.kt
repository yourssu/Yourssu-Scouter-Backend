package com.yourssu.scouter.mail.core.implement

import com.yourssu.scouter.mail.core.implement.MailReservationStatus.PENDING_SEND
import com.yourssu.scouter.mail.core.implement.MailReservationStatus.SCHEDULED
import com.yourssu.scouter.mail.core.implement.MailReservationStatus.SENDING
import com.yourssu.scouter.mail.core.implement.MailReservationStatus.SENT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.Test
import java.util.stream.Stream

@Suppress("NonAsciiCharacters")
class MailReservationGroupTest {
    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("statusCases")
    fun `resolveStatus는 소속 메일 상태들로부터 그룹 상태를 계산한다`(
        statuses: List<MailReservationStatus>,
        expected: MailReservationStatus,
    ) {
        assertThat(MailReservationGroup.resolveStatus(statuses)).isEqualTo(expected)
    }

    @Test
    fun `resolveStatus는 소속 메일이 없으면 null을 반환한다`() {
        assertThat(MailReservationGroup.resolveStatus(emptyList())).isNull()
    }

    companion object {
        @JvmStatic
        fun statusCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf(SCHEDULED, SCHEDULED), SCHEDULED),
                Arguments.of(listOf(SENT, SENT, SENT), SENT),
                Arguments.of(listOf(SENT, SENDING, SCHEDULED), SENDING),
                Arguments.of(listOf(SENDING, PENDING_SEND), SENDING),
                Arguments.of(listOf(SENT, PENDING_SEND), PENDING_SEND),
                Arguments.of(listOf(SCHEDULED, PENDING_SEND), PENDING_SEND),
                Arguments.of(listOf(SENT, SCHEDULED), SCHEDULED),
            )
    }
}
