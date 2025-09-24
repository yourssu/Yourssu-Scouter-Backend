package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncMapping
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.support.google.GoogleFormsReader
import com.yourssu.scouter.common.implement.support.google.UserResponse
import com.yourssu.scouter.common.implement.support.initialization.ApplicantAvailableTimeMap
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Component
class FormResponseToApplicantProcessor(
    private val googleFormsReader: GoogleFormsReader,
    private val applicantAvailableTimeMap: ApplicantAvailableTimeMap
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
            age = userResponse.getAnswer(question.ageQuestion) ?: "",
            department = userResponse.getAnswer(question.departmentQuestion) ?: "",
            studentId = userResponse.getAnswer(question.studentIdQuestion) ?: "",
            part = part,
            state = ApplicantState.UNDER_REVIEW,
            applicationDateTime = userResponse.createTime,
            applicationSemester = applicationSemester,
            academicSemester = userResponse.getAnswer(question.academicSemesterQuestion) ?: "",
            availableTimes = parseResponseToLocalDateTime(
                userResponse, question.availableTimeQuestion
            ),
        )

        return ApplicantSyncInfo(applicant, formId, userResponse.responseId)
    }

    fun mapFormResponsesToApplicants(
        googleAccessToken: String,
        applicantSyncMapping: ApplicantSyncMapping
    ): List<ApplicantSyncInfo> {
        val userResponses: List<UserResponse> = googleFormsReader.getUserResponses(googleAccessToken, applicantSyncMapping.formId)

        return userResponses.map { userResponse ->
            mapResponseToApplicant(userResponse, applicantSyncMapping)
        }
    }

    private fun mapResponseToApplicant(
        userResponse: UserResponse,
        applicantSyncMapping: ApplicantSyncMapping
    ): ApplicantSyncInfo {
        val applicant = Applicant(
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
            availableTimes = parseResponseToLocalDateTime(userResponse, applicantSyncMapping.availableTimeQuestion),
        )

        return ApplicantSyncInfo(applicant, applicantSyncMapping.formId, userResponse.responseId)
    }

    private fun parseResponseToLocalDateTime(
        userResponse: UserResponse, question: String?
    ): List<LocalDateTime> =
        userResponse.getAll(question).let { responseItems ->
            responseItems.flatMap {
                if (it.answer == "불가") return@flatMap emptyList()
                val days = it.question.substringAfterLast(":")
                val times: List<String>? =
                    if (it.answer == "상관없음") applicantAvailableTimeMap.time.flatMap { (_, value) -> value }
                    else {
                        it.answer.split(",").flatMap { time ->
                            applicantAvailableTimeMap.time[time.trim()] as? Iterable<String> ?: emptyList()
                        }
                    }
                val year = LocalDateTime.now().year
                times?.map { time ->
                    LocalDateTime.parse(
                        "$year $days $time",
                        DateTimeFormatter.ofPattern("yyyy M월 d일 E요일 HH:mm").withLocale(Locale.KOREA)
                    )
                } ?: emptyList()
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
