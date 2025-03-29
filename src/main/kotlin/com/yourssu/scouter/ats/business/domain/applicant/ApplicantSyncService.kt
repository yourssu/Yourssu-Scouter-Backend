package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
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

        val successMessages: MutableList<String> = mutableListOf()
        val failureMessages: MutableList<String> = mutableListOf()
        val totalSyncResults: MutableList<ApplicantSyncInfo> = mutableListOf()
        for (form: GoogleDriveFile in forms) {
            val part: Part? = parts.find { form.name.contains(it.name, ignoreCase = true) }
            if (part == null) {
                failureMessages.add("No part found for form: ${form.name}(${form.webViewLink})")
                continue
            }

            val partSyncResults: List<ApplicantSyncInfo> = formResponseProcessor.mapFormResponsesToApplicants(
                googleAccessToken = googleAccessToken,
                formId = form.id,
                applicationSemester = applicationSemester,
                part = part,
                question = MappingQuestionDto(
                    nameQuestion = "성명",
                    phoneNumberQuestion = "연락처",
                    ageQuestion = "나이",
                    departmentQuestion = "학과(부)",
                    studentIdQuestion = "학번",
                    academicSemesterQuestion = "재학중인 학기",
                ),
            )
            if (partSyncResults.isEmpty()) {
                failureMessages.add("No responses for form: ${form.name}(${form.webViewLink})")
                continue
            }
            successMessages.add("Sync completed for form: ${form.name}(${form.webViewLink})")
            totalSyncResults.addAll(partSyncResults)
        }

        writeNewApplicants(applicationSemester, totalSyncResults)

        return ApplicantSyncResult(
            successMessages = successMessages,
            failureMessages = failureMessages,
        )
    }

    fun readLastUpdatedTime(): LocalDateTime? {
        val lastLog: ApplicantSyncLog? = applicantSyncLogReader.findLastLog()

        return lastLog?.syncTime
    }
}
