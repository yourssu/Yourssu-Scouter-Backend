package com.yourssu.scouter.recruiting.applicant.business

import com.yourssu.scouter.common.google.ResponseItem
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSyncMapping
import com.yourssu.scouter.recruiting.support.business.utils.AvailableTimeParser
import com.yourssu.scouter.masterdata.part.implement.fixture.PartFixtureBuilder
import com.yourssu.scouter.masterdata.semester.implement.fixture.SemesterFixtureBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant

@Suppress("NonAsciiCharacters")
class FormResponseToApplicantProcessorTest {

    private val availableTimeParser: AvailableTimeParser = mock(AvailableTimeParser::class.java)
    private lateinit var processor: FormResponseToApplicantProcessor
    private lateinit var mapping: ApplicantSyncMapping

    @BeforeEach
    fun setUp() {
        processor = FormResponseToApplicantProcessor(mock(), availableTimeParser)
        whenever(availableTimeParser.parse(any(), any())).thenReturn(emptyList())

        mapping = ApplicantSyncMapping(
            applicationSemester = SemesterFixtureBuilder().id(1L).build(),
            part = PartFixtureBuilder().id(1L).build(),
            formId = "form-1",
            nameQuestion = "이름",
            emailQuestion = null,
            phoneNumberQuestion = "휴대폰 번호",
            ageQuestion = "나이",
            departmentQuestion = "학과",
            studentIdQuestion = "학번",
            academicSemesterQuestion = "재학중인 학기",
            availableTimeQuestion = null,
        )
    }

    @Test
    fun `웹훅 payload의 매핑 필드로 Applicant를 구성한다`() {
        val items = listOf(
            ResponseItem("이름", "홍길동"),
            ResponseItem("휴대폰 번호", "010-1234-5678"),
            ResponseItem("나이", "25"),
            ResponseItem("학과", "컴퓨터공학부"),
            ResponseItem("학번", "20210001"),
            ResponseItem("재학중인 학기", "3-1"),
        )

        val result = processor.mapWebhookResponseToApplicant(
            responseId = "response-1",
            createTime = Instant.parse("2026-01-01T00:00:00Z"),
            respondentEmail = "hong@example.com",
            items = items,
            applicantSyncMapping = mapping,
        )

        assertThat(result.applicant.name).isEqualTo("홍길동")
        assertThat(result.applicant.email).isEqualTo("hong@example.com")
        assertThat(result.applicant.phoneNumber).isEqualTo("010-1234-5678")
        assertThat(result.formId).isEqualTo("form-1")
        assertThat(result.responseId).isEqualTo("response-1")
    }

    @Test
    fun `매핑 안 된 응답 중 장문형만 unmappedResponseItems 후보로 남긴다`() {
        val items = listOf(
            ResponseItem("이름", "홍길동"),
            ResponseItem("휴대폰 번호", "010-1234-5678"),
            ResponseItem("나이", "25"),
            ResponseItem("학과", "컴퓨터공학부"),
            ResponseItem("학번", "20210001"),
            ResponseItem("재학중인 학기", "3-1"),
            ResponseItem("좋아하는 색은?", "빨강", isDescriptive = false),
            ResponseItem("자기소개를 해주세요", "안녕하세요, 저는...", isDescriptive = true),
        )

        val result = processor.mapWebhookResponseToApplicant(
            responseId = "response-1",
            createTime = Instant.parse("2026-01-01T00:00:00Z"),
            respondentEmail = "hong@example.com",
            items = items,
            applicantSyncMapping = mapping,
        )

        assertThat(result.unmappedResponseItems).hasSize(1)
        assertThat(result.unmappedResponseItems.single().question).isEqualTo("자기소개를 해주세요")
    }
}
