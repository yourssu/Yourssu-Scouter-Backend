package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncLog
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncLogReader
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncLogWriter
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantWriter
import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.business.support.utils.SemesterConverter
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.support.google.GoogleDriveFile
import com.yourssu.scouter.common.implement.support.google.GoogleDriveMimeType
import com.yourssu.scouter.common.implement.support.google.GoogleDriveQueryBuilder
import com.yourssu.scouter.common.implement.support.google.GoogleDriveReader
import com.yourssu.scouter.common.implement.support.google.GoogleFormsReader
import com.yourssu.scouter.common.implement.support.google.UserResponse
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class ApplicantSyncService(
    private val oauth2Service: OAuth2Service,
    private val applicantWriter: ApplicantWriter,
    private val partReader: PartReader,
    private val semesterReader: SemesterReader,
    private val applicantSyncLogReader: ApplicantSyncLogReader,
    private val applicantSyncLogWriter: ApplicantSyncLogWriter,
    private val googleDriveReader: GoogleDriveReader,
    private val googleFormsReader: GoogleFormsReader,
    private val formResponseProcessor: FormResponseToApplicantProcessor,
) {

    fun includeFromForms(
        authUserId: Long,
        command: ApplicantSyncCommand,
    ): ApplicantSyncResult {
        val authUser: User = oauth2Service.refreshOAuth2TokenBeforeExpiry(authUserId, OAuth2Type.GOOGLE, 10L)
        val googleAccessToken: String = authUser.getBearerAccessToken()
        val part: Part = partReader.readById(command.partId)
        val applicationSemester: Semester = semesterReader.readById(command.semesterId)
        val syncResults: List<ApplicantSyncInfo> = formResponseProcessor.mapFormResponsesToApplicants(
            googleAccessToken = googleAccessToken,
            formId = command.formId,
            applicationSemester = applicationSemester,
            part = part,
            question = command.toMappingQuestionDto(),
        )

        if (syncResults.isEmpty()) {
            return ApplicantSyncResult.failure("No responses for form: ${command.formId}")
        }

        writeNewApplicants(applicationSemester, syncResults)

        return ApplicantSyncResult.success("Sync completed for form: ${command.formId}")
    }

    private fun writeNewApplicants(
        applicantSemester: Semester,
        syncResults: List<ApplicantSyncInfo>,
    ) {
        if (syncResults.isEmpty()) {
            return
        }
        val semesterSyncLogs: List<ApplicantSyncLog> =
            applicantSyncLogReader.readAllByApplicantSemesterId(applicantSemester.id!!)

        val newResults: List<ApplicantSyncInfo> = syncResults.filter { syncResult ->
            semesterSyncLogs.none { log ->
                log.formId == syncResult.formId && log.responseId == syncResult.responseId
            }
        }

        val syncDateTime: LocalDateTime = LocalDateTime.now()
        val newApplicants: MutableList<Applicant> = mutableListOf()
        val newSyncLogs: MutableList<ApplicantSyncLog> = mutableListOf()
        for (syncResult in newResults) {
            newApplicants.add(syncResult.applicant)
            newSyncLogs.add(
                ApplicantSyncLog(
                    applicantSemesterId = applicantSemester.id,
                    formId = syncResult.formId,
                    responseId = syncResult.responseId,
                    syncTime = syncDateTime,
                )
            )
        }

        applicantWriter.writeAll(newApplicants)
        applicantSyncLogWriter.writeAll(newSyncLogs)
    }

    fun includeFromForms(
        authUserId: Long,
        targetSemester: String? = null,
    ): ApplicantSyncResult {
        val authUser: User = oauth2Service.refreshOAuth2TokenBeforeExpiry(authUserId, OAuth2Type.GOOGLE, 10L)
        val googleAccessToken: String = authUser.getBearerAccessToken()
        val applicationSemesterString = targetSemester ?: SemesterConverter.convertToIntString(LocalDate.now())
        val query: String = GoogleDriveQueryBuilder()
            .nameContainsAll("지원서", applicationSemesterString)
            .exceptNameContainsAll("양식")
            .mimeType(GoogleDriveMimeType.FORM)
            .build()

        val forms: List<GoogleDriveFile> = googleDriveReader.getFiles(googleAccessToken, query)
        val parts: List<Part> = partReader.readAll()
        val applicationSemester: Semester = semesterReader.readByString(applicationSemesterString)

        val successMessages = mutableListOf<String>()
        val failureMessages = mutableListOf<String>()

        val totalSyncResults: MutableList<SingleResponseSyncResult> = mutableListOf()
        for (form: GoogleDriveFile in forms) {
            val partSyncResults: List<SingleResponseSyncResult> = extractApplicantsFromForm(
                form = form,
                googleAccessToken = googleAccessToken,
                parts = parts,
                applicationSemester = applicationSemester,
                successMessages = successMessages,
                failureMessages = failureMessages,
            )
            totalSyncResults.addAll(partSyncResults)
        }

        writeNewApplicants(applicationSemester, totalSyncResults)

        return ApplicantSyncResult(
            successMessages = successMessages,
            failureMessages = failureMessages,
        )
    }

    private fun writeNewApplicants(
        applicantSemester: Semester,
        syncResults: List<SingleResponseSyncResult>,
    ) {
        if (syncResults.isEmpty()) {
            return
        }
        val syncDateTime: LocalDateTime = LocalDateTime.now()
        val newApplicants: MutableList<Applicant> = mutableListOf()
        val newSyncLogs: MutableList<ApplicantSyncLog> = mutableListOf()
        val semesterSyncLogs: List<ApplicantSyncLog> =
            applicantSyncLogReader.readAllByApplicantSemesterId(applicantSemester.id!!)

        for (syncResult: SingleResponseSyncResult in syncResults) {
            val syncLog: ApplicantSyncLog? = semesterSyncLogs.find {log ->
                (log.formId == syncResult.formId) && (log.responseId == syncResult.responseId)
            }
            if (syncLog != null) {
                continue
            }

            newApplicants.add(syncResult.applicant)
            newSyncLogs.add(
                ApplicantSyncLog(
                    applicantSemesterId = applicantSemester.id,
                    formId = syncResult.formId,
                    responseId = syncResult.responseId,
                    syncTime = syncDateTime,
                )
            )
        }

        applicantWriter.writeAll(newApplicants)
        applicantSyncLogWriter.writeAll(newSyncLogs)
    }

    private fun extractApplicantsFromForm(
        form: GoogleDriveFile,
        googleAccessToken: String,
        parts: List<Part>,
        applicationSemester: Semester,
        successMessages: MutableList<String>,
        failureMessages: MutableList<String>,
    ): List<SingleResponseSyncResult> {
        val userResponses: List<UserResponse> = googleFormsReader.getUserResponses(googleAccessToken, form.id)
        if (userResponses.isEmpty()) {
            failureMessages.add("No responses for form: ${form.name}(${form.webViewLink})")
            return emptyList()
        }

        val part: Part? = parts.find { normalizeString(form.name).contains(normalizeString(it.name)) }
        if (part == null) {
            failureMessages.add("No part found for form: ${form.name}(${form.webViewLink})")
            return emptyList()
        }

        val formSyncResult: List<SingleResponseSyncResult> = userResponses.map { userResponse ->
            mapResponseToApplicant(form.id, userResponse, part, applicationSemester)
        }

        successMessages.add(
            "'${form.name}'의 ${userResponses.size}개의 응답 중 ${formSyncResult.size}명의 지원자를 추출했습니다."
        )

        return formSyncResult
    }

    private fun normalizeString(value: String): String = value.replace(" ", "").lowercase()

    private fun mapResponseToApplicant(
        formId: String,
        userResponse: UserResponse,
        part: Part,
        applicationSemester: Semester
    ): SingleResponseSyncResult {
        val responseMap = userResponse.responseItems.associate { normalizeString(it.question) to it.answer }

        val applicant = Applicant(
            name = responseMap["이름"] ?: responseMap["성함"] ?: "",
            email = userResponse.respondentEmail ?: "",
            phoneNumber = responseMap["연락처"] ?: "",
            age = responseMap["나이"] ?: "",
            department = responseMap["학과(부)"] ?: "",
            studentId = responseMap["학번"] ?: "",
            part = part,
            state = ApplicantState.UNDER_REVIEW,
            applicationDateTime = userResponse.createTime,
            applicationSemester = applicationSemester,
            academicSemester = responseMap.entries.firstOrNull { it.key.contains("재학중인학기") }?.value ?: ""
        )

        return SingleResponseSyncResult(applicant, formId, userResponse.responseId)
    }

    fun readLastUpdatedTime(): LocalDateTime? {
        val lastLog: ApplicantSyncLog? = applicantSyncLogReader.findLastLog()

        return lastLog?.syncTime
    }
}

class SingleResponseSyncResult(
    val applicant: Applicant,
    val formId: String,
    val responseId: String,
)
