package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.business.support.utils.SemesterConverter
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.support.google.GoogleDriveFile
import com.yourssu.scouter.common.implement.support.google.GoogleDriveMimeType
import com.yourssu.scouter.common.implement.support.google.GoogleDriveQueryBuilder
import com.yourssu.scouter.common.implement.support.google.GoogleDriveReader
import com.yourssu.scouter.common.implement.support.google.GoogleFormsReader
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class MemberSyncService(
    private val applicantReader: ApplicantReader,
    private val departmentReader: DepartmentReader,
    private val memberService: MemberService,
    private val oauth2Service: OAuth2Service,
    private val googleDriveReader: GoogleDriveReader,
    private val googleFormsReader: GoogleFormsReader,
) {

    fun includeAcceptedApplicants(
        authUserId: Long,
        targetSemester: String? = null,
    ): MemberSyncResult {
        val acceptedApplicants: List<Applicant> = applicantReader.filterByState(ApplicantState.FINAL_ACCEPTED)
        val authUser: User = oauth2Service.refreshOAuth2TokenBeforeExpiry(authUserId, OAuth2Type.GOOGLE, 10L)
        val googleAccessToken: String = authUser.getBearerAccessToken()
        val targetSemesterString = targetSemester ?: SemesterConverter.convertToIntString(LocalDate.now())
        val query: String = GoogleDriveQueryBuilder()
            .nameContainsAll("면접 합격자 정보 입력 서베이", targetSemesterString)
            .mimeType(GoogleDriveMimeType.FORM)
            .build()

        val forms: List<GoogleDriveFile> = googleDriveReader.getFiles(googleAccessToken, query)
        val additionalInfos = processForms(googleAccessToken, forms)

        val failureMessages: List<String> =
            mergeToActiveMemberAndReturnFailMessages(acceptedApplicants, additionalInfos)

        return MemberSyncResult(failureMessages)
    }

    private fun mergeToActiveMemberAndReturnFailMessages(
        acceptedApplicants: List<Applicant>,
        additionalInfos: List<AcceptedApplicantResponse>,
    ): List<String> {
        val departments: List<Department> = departmentReader.readAll()
        val failureMessages = mutableListOf<String>()
        val acceptedApplicantsMap = acceptedApplicants.associateBy { it.studentId }
        val acceptedResponseMap = additionalInfos.associateBy { it.studentId }
        for ((studentId, applicant) in acceptedApplicantsMap) {
            val additionalInfo: AcceptedApplicantResponse? = acceptedResponseMap[studentId]
            if (additionalInfo == null) {
                failureMessages.add("${applicant.name}(${applicant.studentId}) - 합격자 정보 입력 서베이 응답 X")
                continue
            }
            val department: Department? =
                departments.find { normalizeString(applicant.department).contains(normalizeString(it.name)) }
            if (department == null) {
                failureMessages.add("${applicant.name}(${applicant.studentId}) - [${applicant.department}]에 해당하는 학과가 존재하지 않음")
                continue
            }

            val newMember = Member(
                name = applicant.name,
                email = additionalInfo.yourssuEmail,
                phoneNumber = applicant.phoneNumber,
                birthDate = additionalInfo.birthDate,
                department = department,
                studentId = applicant.studentId,
                parts = sortedSetOf(applicant.part),
                role = MemberRole.MEMBER,
                nicknameEnglish = NicknameConverter.extractNickname(additionalInfo.nickname),
                nicknameKorean = NicknameConverter.extractPronunciation(additionalInfo.nickname),
                state = MemberState.ACTIVE,
                joinDate = LocalDate.now(),
                note = "",
                stateUpdatedTime = LocalDateTime.now(),
            )

            memberService.createMemberWithActiveStateIfNotExists(newMember)
        }

        return failureMessages
    }

    private fun processForms(
        googleAccessToken: String,
        forms: List<GoogleDriveFile>
    ): List<AcceptedApplicantResponse> {
        return forms.map { form ->
            mapResponsesToAdditionalInfos(googleAccessToken, form)
        }.flatten()
    }

    private fun mapResponsesToAdditionalInfos(
        googleAccessToken: String,
        form: GoogleDriveFile
    ): List<AcceptedApplicantResponse> {
        return googleFormsReader.getUserResponses(googleAccessToken, form.id)
            .map { userResponse ->
                val responseMap = userResponse.responseItems.associate { it.question to it.answer }
                AcceptedApplicantResponse(
                    studentId = responseMap.entries.firstOrNull { it.key.contains("학번") }?.value ?: "",
                    nickname = responseMap.entries.firstOrNull { it.key.contains("닉네임") }?.value ?: "",
                    yourssuEmail = responseMap.entries.firstOrNull { it.key.contains("메일") }?.value ?: "",
                    birthDate = LocalDate.parse(
                        responseMap.entries.firstOrNull { it.key.contains("생일") }?.value ?: ""
                    )
                )
            }
    }

    private fun normalizeString(value: String): String = value.replace(" ", "").lowercase()
}

data class AcceptedApplicantResponse(
    val studentId: String,
    val nickname: String,
    val yourssuEmail: String,
    val birthDate: LocalDate,
)
