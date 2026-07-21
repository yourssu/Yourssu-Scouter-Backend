package com.yourssu.scouter.recruiting.applicant.business

import com.yourssu.scouter.recruiting.applicant.implement.*
import com.yourssu.scouter.auth.authentication.business.OAuth2Service
import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.common.semester.implement.SemesterReader
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.common.support.implement.google.ResponseItem
import com.yourssu.scouter.recruiting.rubric.business.DocumentSectionService
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSection
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate

@Service
class ApplicantSyncService(
    private val oauth2Service: OAuth2Service,
    private val applicantWriter: ApplicantWriter,
    private val applicantAnswerWriter: ApplicantAnswerWriter,
    private val semesterReader: SemesterReader,
    private val applicantSyncLogReader: ApplicantSyncLogReader,
    private val applicantSyncLogWriter: ApplicantSyncLogWriter,
    private val applicantSyncMappingReader: ApplicantSyncMappingReader,
    private val formResponseProcessor: FormResponseToApplicantProcessor,
    private val documentSectionService: DocumentSectionService,
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
            try {
                val syncResults: List<ApplicantSyncInfo> = formResponseProcessor.mapFormResponsesToApplicants(
                    googleAccessToken = googleAccessToken,
                    applicantSyncMapping = syncMapping,
                )

                if (syncResults.isEmpty()) {
                    failureMessages.add("No responses for form: ${syncMapping.formId}")
                }

                successMessages.add("Sync completed for form: ${syncMapping.formId}")
                totalSyncResults.addAll(syncResults)
            } catch (e: Exception) {
                failureMessages.add("Failed to sync form ${syncMapping.formId}: ${e.message}")
            }
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

        val syncDateTime: Instant = Instant.now()
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

        val savedApplicants: List<Applicant> = applicantWriter.writeAll(newApplicants)
        writeUnmappedAnswers(savedApplicants, newResults)
        applicantSyncLogWriter.writeAll(newSyncLogs)
    }

    private fun writeUnmappedAnswers(
        savedApplicants: List<Applicant>,
        syncResults: List<ApplicantSyncInfo>,
    ) {
        val pairs = savedApplicants.zip(syncResults)
        val sectionsByPartIdAndQuestion = syncDocumentSections(pairs)

        val newAnswers: List<ApplicantAnswer> = pairs.flatMap { (saved, syncResult) ->
            syncResult.unmappedResponseItems.map { item ->
                ApplicantAnswer(
                    applicantId = saved.id!!,
                    sectionId = sectionsByPartIdAndQuestion[saved.part.id!! to item.question]?.id,
                    question = item.question,
                    answer = item.answer,
                )
            }
        }

        applicantAnswerWriter.writeAll(newAnswers)
    }

    // 서류 평가 문항은 파트별로 관리되므로, 서술형 질문을 파트별로 모아 DocumentSection을 동기화한다 [B1].
    private fun syncDocumentSections(
        pairs: List<Pair<Applicant, ApplicantSyncInfo>>,
    ): Map<Pair<Long, String>, DocumentSection> {
        val descriptiveQuestionsByPartId: Map<Long, Set<String>> = pairs
            .flatMap { (saved, syncResult) ->
                syncResult.unmappedResponseItems
                    .filter(ResponseItem::isDescriptive)
                    .map { saved.part.id!! to it.question }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.toSet() }

        return descriptiveQuestionsByPartId
            .flatMap { (partId, questions) ->
                documentSectionService.syncSectionsFromQuestions(partId, questions)
                    .map { (question, section) -> (partId to question) to section }
            }
            .toMap()
    }

    fun readLastUpdatedTime(): Instant? {
        val lastLog: ApplicantSyncLog? = applicantSyncLogReader.findLastLog()

        return lastLog?.syncTime
    }
}
