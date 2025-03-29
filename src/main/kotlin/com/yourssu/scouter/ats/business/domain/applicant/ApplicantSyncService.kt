package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncLog
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncLogReader
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncLogWriter
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncMapping
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncMappingReader
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantWriter
import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import com.yourssu.scouter.common.implement.domain.user.User
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class ApplicantSyncService(
    private val oauth2Service: OAuth2Service,
    private val applicantWriter: ApplicantWriter,
    private val semesterReader: SemesterReader,
    private val applicantSyncLogReader: ApplicantSyncLogReader,
    private val applicantSyncLogWriter: ApplicantSyncLogWriter,
    private val applicantSyncMappingReader: ApplicantSyncMappingReader,
    private val formResponseProcessor: FormResponseToApplicantProcessor,
) {

    fun includeFromForms(
        authUserId: Long,
        applicationSemesterId: Long? = null,
    ): ApplicantSyncResult {
        val targetApplicationSemesterId: Long = applicationSemesterId ?: semesterReader.readByDate(LocalDate.now()).id!!
        val authUser: User = oauth2Service.refreshOAuth2TokenBeforeExpiry(authUserId, OAuth2Type.GOOGLE, 10L)
        val googleAccessToken: String = authUser.getBearerAccessToken()
        val syncMappings: List<ApplicantSyncMapping> =
            applicantSyncMappingReader.readAllByApplicationSemesterId(targetApplicationSemesterId)

        val successMessages: MutableList<String> = mutableListOf()
        val failureMessages: MutableList<String> = mutableListOf()
        val totalSyncResults: MutableList<ApplicantSyncInfo> = mutableListOf()
        for (syncMapping: ApplicantSyncMapping in syncMappings) {
            val syncResults: List<ApplicantSyncInfo> = formResponseProcessor.mapFormResponsesToApplicants(
                googleAccessToken = googleAccessToken,
                applicantSyncMapping = syncMapping,
            )

            if (syncResults.isEmpty()) {
                failureMessages.add("No responses for form: ${syncMapping.formId}")
            }

            successMessages.add("Sync completed for form: ${syncMapping.formId}")
            totalSyncResults.addAll(syncResults)
        }

        writeNewApplicants(targetApplicationSemesterId, totalSyncResults)

        return ApplicantSyncResult(
            successMessages = successMessages,
            failureMessages = failureMessages,
        )
    }

    private fun writeNewApplicants(
        applicationSemesterId: Long,
        syncResults: List<ApplicantSyncInfo>,
    ) {
        if (syncResults.isEmpty()) {
            return
        }

        val semesterSyncLogs: List<ApplicantSyncLog> =
            applicantSyncLogReader.readAllByApplicationSemesterId(applicationSemesterId)

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
                    applicationSemesterId = applicationSemesterId,
                    formId = syncResult.formId,
                    responseId = syncResult.responseId,
                    syncTime = syncDateTime,
                )
            )
        }

        applicantWriter.writeAll(newApplicants)
        applicantSyncLogWriter.writeAll(newSyncLogs)
    }

    fun readLastUpdatedTime(): LocalDateTime? {
        val lastLog: ApplicantSyncLog? = applicantSyncLogReader.findLastLog()

        return lastLog?.syncTime
    }
}
