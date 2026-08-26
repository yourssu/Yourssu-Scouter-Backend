package com.yourssu.scouter.recruiting.applicant.business

import com.yourssu.scouter.recruiting.support.business.utils.AgeNormalizer
import com.yourssu.scouter.recruiting.support.business.utils.AvailableTimeParser
import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSyncMapping
import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.common.google.GoogleFormsReader
import com.yourssu.scouter.common.google.ResponseItem
import com.yourssu.scouter.common.google.UserResponse
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class FormResponseToApplicantProcessor(
    private val googleFormsReader: GoogleFormsReader,
    private val availableTimeParser: AvailableTimeParser,
) {
    fun mapFormResponsesToApplicants(
        googleAccessToken: String,
        formId: String,
        applicationSemester: Semester,
        part: Part,
        question: MappingQuestionDto,
    ): List<ApplicantSyncInfo> {
        val userResponses: List<UserResponse> = googleFormsReader.getUserResponses(googleAccessToken, formId)

        return userResponses.map { singleResponse ->
            mapResponseToApplicant(
                formId = formId,
                userResponse = singleResponse,
                applicationSemester = applicationSemester,
                part = part,
                question = question,
            )
        }
    }

    private fun mapResponseToApplicant(
        formId: String,
        userResponse: UserResponse,
        applicationSemester: Semester,
        part: Part,
        question: MappingQuestionDto,
    ): ApplicantSyncInfo {
        val applicant = Applicant(
            name = userResponse.getAnswer(question.nameQuestion) ?: "",
            email = userResponse.getAnswer(question.emailQuestion) ?: userResponse.respondentEmail ?: "",
            phoneNumber = userResponse.getAnswer(question.phoneNumberQuestion) ?: "",
            age = AgeNormalizer.normalize(userResponse.getAnswer(question.ageQuestion)),
            department = userResponse.getAnswer(question.departmentQuestion) ?: "",
            studentId = userResponse.getAnswer(question.studentIdQuestion) ?: "",
            part = part,
            state = ApplicantState.UNDER_REVIEW,
            applicationDateTime = userResponse.createTime,
            applicationSemester = applicationSemester,
            academicSemester = userResponse.getAnswer(question.academicSemesterQuestion) ?: "",
            availableTimes = availableTimeParser.parse(
                responseItems = userResponse.responseItems,
                availableTimeQuestion = question.availableTimeQuestion,
            ),
        )

        return ApplicantSyncInfo(applicant, formId, userResponse.responseId)
    }

    fun mapFormResponsesToApplicants(
        googleAccessToken: String,
        applicantSyncMapping: ApplicantSyncMapping,
    ): List<ApplicantSyncInfo> {
        val userResponses: List<UserResponse> = googleFormsReader.getUserResponses(googleAccessToken, applicantSyncMapping.formId)

        return userResponses.map { userResponse ->
            mapResponseToApplicant(userResponse, applicantSyncMapping)
        }
    }

    private fun mapResponseToApplicant(
        userResponse: UserResponse,
        applicantSyncMapping: ApplicantSyncMapping,
    ): ApplicantSyncInfo {
        val applicant = Applicant(
            name = userResponse.getAnswer(applicantSyncMapping.nameQuestion) ?: "",
            email = userResponse.getAnswer(applicantSyncMapping.emailQuestion) ?: userResponse.respondentEmail ?: "",
            phoneNumber = userResponse.getAnswer(applicantSyncMapping.phoneNumberQuestion) ?: "",
            age = AgeNormalizer.normalize(userResponse.getAnswer(applicantSyncMapping.ageQuestion)),
            department = userResponse.getAnswer(applicantSyncMapping.departmentQuestion) ?: "",
            studentId = userResponse.getAnswer(applicantSyncMapping.studentIdQuestion) ?: "",
            part = applicantSyncMapping.part,
            state = ApplicantState.UNDER_REVIEW,
            applicationDateTime = userResponse.createTime,
            applicationSemester = applicantSyncMapping.applicationSemester,
            academicSemester = userResponse.getAnswer(applicantSyncMapping.academicSemesterQuestion) ?: "",
            availableTimes = availableTimeParser.parse(
                responseItems = userResponse.responseItems,
                availableTimeQuestion = applicantSyncMapping.availableTimeQuestion,
            ),
        )

        return ApplicantSyncInfo(
            applicant = applicant,
            formId = applicantSyncMapping.formId,
            responseId = userResponse.responseId,
            unmappedResponseItems = extractUnmappedResponseItems(userResponse, applicantSyncMapping),
        )
    }

    // Apps Script가 폼 제출 즉시 보내는 웹훅 payload를 UserResponse로 감싸 pull-sync와 동일한 매핑 로직을 재사용한다.
    fun mapWebhookResponseToApplicant(
        responseId: String,
        createTime: Instant,
        respondentEmail: String?,
        items: List<ResponseItem>,
        applicantSyncMapping: ApplicantSyncMapping,
    ): ApplicantSyncInfo {
        val userResponse = UserResponse(
            responseId = responseId,
            createTime = createTime,
            respondentEmail = respondentEmail,
            lastSubmittedTime = null,
            responseItems = items,
        )
        return mapResponseToApplicant(userResponse, applicantSyncMapping)
    }

    private fun extractUnmappedResponseItems(
        userResponse: UserResponse,
        applicantSyncMapping: ApplicantSyncMapping,
    ): List<ResponseItem> {
        // getAnswer/getAll과 동일하게 startsWith 기준으로 매핑 여부를 판단한다.
        // availableTimeQuestion은 날짜별로 독립된 문항이 여러 개 존재하지만 모두 같은 접두어로 시작하므로 함께 제외된다.
        val mappedQuestions: List<String> = listOfNotNull(
            applicantSyncMapping.nameQuestion,
            applicantSyncMapping.emailQuestion,
            applicantSyncMapping.phoneNumberQuestion,
            applicantSyncMapping.ageQuestion,
            applicantSyncMapping.departmentQuestion,
            applicantSyncMapping.studentIdQuestion,
            applicantSyncMapping.academicSemesterQuestion,
            applicantSyncMapping.availableTimeQuestion,
        )

        // 장문형(서술형) 응답만 서류 평가 문항(ApplicantAnswer/DocumentSection) 후보로 저장한다.
        return userResponse.responseItems.filter { item ->
            item.isDescriptive &&
                item.answer.isNotBlank() &&
                mappedQuestions.none { question -> item.question.startsWith(question) }
        }
    }
}

data class MappingQuestionDto(
    val nameQuestion: String?,
    val emailQuestion: String?,
    val phoneNumberQuestion: String?,
    val ageQuestion: String?,
    val departmentQuestion: String?,
    val studentIdQuestion: String,
    val academicSemesterQuestion: String?,
    val availableTimeQuestion: String?,
)
