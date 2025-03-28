package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.support.google.GoogleFormsReader
import com.yourssu.scouter.common.implement.support.google.UserResponse
import org.springframework.stereotype.Component

@Component
class FormResponseToApplicantProcessor(
    private val googleFormsReader: GoogleFormsReader,
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
            email = userResponse.respondentEmail ?: "",
            phoneNumber = userResponse.getAnswer(question.phoneNumberQuestion) ?: "",
            age = userResponse.getAnswer(question.ageQuestion) ?: "",
            department = userResponse.getAnswer(question.departmentQuestion) ?: "",
            studentId = userResponse.getAnswer(question.studentIdQuestion) ?: "",
            part = part,
            state = ApplicantState.UNDER_REVIEW,
            applicationDateTime = userResponse.createTime,
            applicationSemester = applicationSemester,
            academicSemester = userResponse.getAnswer(question.academicSemesterQuestion) ?: "",
        )

        return ApplicantSyncInfo(applicant, formId, userResponse.responseId)
    }
}

data class MappingQuestionDto(
    val nameQuestion: String?,
    val phoneNumberQuestion: String?,
    val ageQuestion: String?,
    val departmentQuestion: String?,
    val studentIdQuestion: String,
    val academicSemesterQuestion: String?,
)
