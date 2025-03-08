package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
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
import org.springframework.stereotype.Service

@Service
class ApplicantSyncService(
    private val oauth2Service: OAuth2Service,
    private val applicantWriter: ApplicantWriter,
    private val partReader: PartReader,
    private val semesterReader: SemesterReader,
    private val googleDriveReader: GoogleDriveReader,
    private val googleFormsReader: GoogleFormsReader,
) {

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

        val totalApplicants: MutableList<Applicant> = mutableListOf()
        for (form in forms) {
            val partApplicants: List<Applicant> = extractApplicantsFromForm(
                form = form,
                googleAccessToken = googleAccessToken,
                parts = parts,
                applicationSemester = applicationSemester,
                successMessages = successMessages,
                failureMessages = failureMessages,
            )
            totalApplicants.addAll(partApplicants)
        }

        applicantWriter.writeAll(totalApplicants)

        return ApplicantSyncResult(
            successeMessages = successMessages,
            failureMessages = failureMessages,
        )
    }

    private fun extractApplicantsFromForm(
        form: GoogleDriveFile,
        googleAccessToken: String,
        parts: List<Part>,
        applicationSemester: Semester,
        successMessages: MutableList<String>,
        failureMessages: MutableList<String>,
    ): List<Applicant> {
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

        val applicants: List<Applicant> = userResponses.map { userResponse ->
            mapResponseToApplicant(userResponse, part, applicationSemester)
        }

        successMessages.add("'${form.name}'의 ${userResponses.size}개의 응답 중 ${applicants.size}명의 지원자를 추출했습니다.")

        return applicants
    }

    private fun normalizeString(value: String): String = value.replace(" ", "").lowercase()

    private fun mapResponseToApplicant(
        userResponse: UserResponse,
        part: Part,
        applicationSemester: Semester
    ): Applicant {
        val responseMap = userResponse.responseItems.associate { normalizeString(it.question) to it.answer }

        return Applicant(
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
    }
}
