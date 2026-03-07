package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.business.support.utils.AvailableTimeParser
import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncMapping
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.support.google.GoogleFormsReader
import com.yourssu.scouter.common.implement.support.google.UserResponse
import org.springframework.stereotype.Component

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
        val applicant =
            Applicant(
                name = userResponse.getAnswer(question.nameQuestion) ?: "",
                email = userResponse.getAnswer(question.emailQuestion) ?: userResponse.respondentEmail ?: "",
                phoneNumber = userResponse.getAnswer(question.phoneNumberQuestion) ?: "",
                age = userResponse.getAnswer(question.ageQuestion) ?: "",
                department = userResponse.getAnswer(question.departmentQuestion) ?: "",
                studentId = userResponse.getAnswer(question.studentIdQuestion) ?: "",
                part = part,
                state = ApplicantState.UNDER_REVIEW,
                applicationDateTime = userResponse.createTime,
                applicationSemester = applicationSemester,
                academicSemester = userResponse.getAnswer(question.academicSemesterQuestion) ?: "",
                availableTimes =
                    availableTimeParser.parse(
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
        val applicant =
            Applicant(
                name = userResponse.getAnswer(applicantSyncMapping.nameQuestion) ?: "",
                email = userResponse.getAnswer(applicantSyncMapping.emailQuestion) ?: userResponse.respondentEmail ?: "",
                phoneNumber = userResponse.getAnswer(applicantSyncMapping.phoneNumberQuestion) ?: "",
                age = userResponse.getAnswer(applicantSyncMapping.ageQuestion) ?: "",
                department = userResponse.getAnswer(applicantSyncMapping.departmentQuestion) ?: "",
                studentId = userResponse.getAnswer(applicantSyncMapping.studentIdQuestion) ?: "",
                part = applicantSyncMapping.part,
                state = ApplicantState.UNDER_REVIEW,
                applicationDateTime = userResponse.createTime,
                applicationSemester = applicantSyncMapping.applicationSemester,
                academicSemester = userResponse.getAnswer(applicantSyncMapping.academicSemesterQuestion) ?: "",
                availableTimes =
                    availableTimeParser.parse(
                        responseItems = userResponse.responseItems,
                        availableTimeQuestion = applicantSyncMapping.availableTimeQuestion,
                    ),
            )

        return ApplicantSyncInfo(applicant, applicantSyncMapping.formId, userResponse.responseId)
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
