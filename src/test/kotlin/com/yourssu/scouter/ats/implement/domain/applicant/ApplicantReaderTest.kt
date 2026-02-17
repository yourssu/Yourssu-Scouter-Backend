package com.yourssu.scouter.ats.implement.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.fixture.ApplicantFixtureBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class ApplicantReaderTest {

    private lateinit var applicantReader: ApplicantReader
    private lateinit var applicantRepository: ApplicantRepository

    @BeforeEach
    fun setUp() {
        applicantRepository = mock(ApplicantRepository::class.java)
        applicantReader = ApplicantReader(applicantRepository)
    }

    @Nested
    @DisplayName("readByPartIdUnderReview 메서드는")
    inner class ReadByPartIdUnderReviewTests {

        @Test
        fun `UNDER_REVIEW 상태의 지원자만 반환한다`() {
            // given
            val partId = 1L
            val underReviewApplicant = ApplicantFixtureBuilder()
                .id(1L)
                .name("심사중")
                .state(ApplicantState.UNDER_REVIEW)
                .build()

            whenever(applicantRepository.findAllByPartIdAndState(partId, ApplicantState.UNDER_REVIEW))
                .thenReturn(listOf(underReviewApplicant))

            // when
            val result = applicantReader.readByPartIdUnderReview(partId)

            // then
            assertThat(result).hasSize(1)
            assertThat(result[0].name).isEqualTo("심사중")
            assertThat(result[0].state).isEqualTo(ApplicantState.UNDER_REVIEW)
        }

        @Test
        fun `해당 파트에 UNDER_REVIEW 상태의 지원자가 없으면 빈 리스트를 반환한다`() {
            // given
            val partId = 1L
            whenever(applicantRepository.findAllByPartIdAndState(partId, ApplicantState.UNDER_REVIEW))
                .thenReturn(emptyList())

            // when
            val result = applicantReader.readByPartIdUnderReview(partId)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        fun `UNDER_REVIEW 상태와 partId를 정확히 전달하여 조회한다`() {
            // given
            val partId = 5L
            whenever(applicantRepository.findAllByPartIdAndState(partId, ApplicantState.UNDER_REVIEW))
                .thenReturn(emptyList())

            // when
            applicantReader.readByPartIdUnderReview(partId)

            // then
            verify(applicantRepository).findAllByPartIdAndState(partId, ApplicantState.UNDER_REVIEW)
        }
    }
}
