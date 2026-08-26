package com.yourssu.scouter.recruiting.applicant.business

import com.yourssu.scouter.auth.authentication.business.OAuth2Service
import com.yourssu.scouter.masterdata.part.implement.fixture.PartFixtureBuilder
import com.yourssu.scouter.masterdata.semester.implement.fixture.SemesterFixtureBuilder
import com.yourssu.scouter.masterdata.semester.implement.SemesterReader
import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantWebhookCommand
import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantWebhookItemCommand
import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantAnswerWriter
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSyncLog
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSyncLogReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSyncLogWriter
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSyncMapping
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSyncMappingReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantWriter
import com.yourssu.scouter.recruiting.applicant.implement.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.recruiting.rubric.business.DocumentSectionService
import com.yourssu.scouter.recruiting.support.implement.exception.ApplicantSyncMappingNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.Instant

@Suppress("NonAsciiCharacters")
class ApplicantSyncServiceTest {

    private val oauth2Service: OAuth2Service = mock(OAuth2Service::class.java)
    private val applicantWriter: ApplicantWriter = mock(ApplicantWriter::class.java)
    private val applicantAnswerWriter: ApplicantAnswerWriter = mock(ApplicantAnswerWriter::class.java)
    private val semesterReader: SemesterReader = mock(SemesterReader::class.java)
    private val applicantSyncLogReader: ApplicantSyncLogReader = mock(ApplicantSyncLogReader::class.java)
    private val applicantSyncLogWriter: ApplicantSyncLogWriter = mock(ApplicantSyncLogWriter::class.java)
    private val applicantSyncMappingReader: ApplicantSyncMappingReader = mock(ApplicantSyncMappingReader::class.java)
    private val formResponseProcessor: FormResponseToApplicantProcessor = mock(FormResponseToApplicantProcessor::class.java)
    private val documentSectionService: DocumentSectionService = mock(DocumentSectionService::class.java)

    private lateinit var applicantSyncService: ApplicantSyncService
    private lateinit var mapping: ApplicantSyncMapping

    @BeforeEach
    fun setUp() {
        applicantSyncService = ApplicantSyncService(
            oauth2Service,
            applicantWriter,
            applicantAnswerWriter,
            semesterReader,
            applicantSyncLogReader,
            applicantSyncLogWriter,
            applicantSyncMappingReader,
            formResponseProcessor,
            documentSectionService,
        )

        mapping = ApplicantSyncMapping(
            applicationSemester = SemesterFixtureBuilder().id(1L).build(),
            part = PartFixtureBuilder().id(1L).build(),
            formId = "form-1",
            nameQuestion = "이름",
            emailQuestion = null,
            phoneNumberQuestion = null,
            ageQuestion = null,
            departmentQuestion = null,
            studentIdQuestion = "학번",
            academicSemesterQuestion = null,
            availableTimeQuestion = null,
        )
    }

    private fun webhookCommand() = ApplicantWebhookCommand(
        formId = "form-1",
        responseId = "response-1",
        createTime = Instant.parse("2026-01-01T00:00:00Z"),
        respondentEmail = "hong@example.com",
        items = listOf(ApplicantWebhookItemCommand("이름", "홍길동", false)),
    )

    @Test
    fun `formId에 해당하는 매핑이 없으면 예외를 던진다`() {
        whenever(applicantSyncMappingReader.readByFormId("form-1"))
            .thenThrow(ApplicantSyncMappingNotFoundException("해당 formId에 대한 동기화 매핑이 존재하지 않습니다: form-1"))

        assertThatThrownBy { applicantSyncService.includeFromWebhook(webhookCommand()) }
            .isInstanceOf(ApplicantSyncMappingNotFoundException::class.java)
    }

    @Test
    fun `동일한 formId, responseId 로그가 이미 있으면 지원자를 저장하지 않는다`() {
        val applicant = ApplicantFixtureBuilder().name("홍길동").build()
        val syncInfo = ApplicantSyncInfo(applicant, "form-1", "response-1")

        whenever(applicantSyncMappingReader.readByFormId("form-1")).thenReturn(mapping)
        whenever(
            formResponseProcessor.mapWebhookResponseToApplicant(
                responseId = any(),
                createTime = any(),
                respondentEmail = any(),
                items = any(),
                applicantSyncMapping = any(),
            )
        ).thenReturn(syncInfo)
        whenever(applicantSyncLogReader.readAllByApplicationSemesterId(1L)).thenReturn(
            listOf(
                ApplicantSyncLog(
                    applicationSemesterId = 1L,
                    formId = "form-1",
                    responseId = "response-1",
                    syncTime = Instant.now(),
                )
            )
        )

        applicantSyncService.includeFromWebhook(webhookCommand())

        verify(applicantWriter).writeAll(emptyList())
        verify(applicantSyncLogWriter).writeAll(emptyList())
    }

    @Test
    fun `새 응답이면 지원자와 동기화 로그를 저장한다`() {
        val applicant = ApplicantFixtureBuilder().name("홍길동").build()
        val savedApplicant = ApplicantFixtureBuilder().id(10L).name("홍길동").build()
        val syncInfo = ApplicantSyncInfo(applicant, "form-1", "response-1")

        whenever(applicantSyncMappingReader.readByFormId("form-1")).thenReturn(mapping)
        whenever(
            formResponseProcessor.mapWebhookResponseToApplicant(
                responseId = any(),
                createTime = any(),
                respondentEmail = any(),
                items = any(),
                applicantSyncMapping = any(),
            )
        ).thenReturn(syncInfo)
        whenever(applicantSyncLogReader.readAllByApplicationSemesterId(1L)).thenReturn(emptyList())
        whenever(applicantWriter.writeAll(listOf(applicant))).thenReturn(listOf(savedApplicant))

        val result = applicantSyncService.includeFromWebhook(webhookCommand())

        verify(applicantWriter).writeAll(listOf(applicant))
        verify(applicantSyncLogWriter).writeAll(any())
        verify(applicantAnswerWriter).writeAll(emptyList())
        assertThat(result.failureMessages).isEmpty()
        assertThat(result.successMessages).hasSize(1)
    }
}
