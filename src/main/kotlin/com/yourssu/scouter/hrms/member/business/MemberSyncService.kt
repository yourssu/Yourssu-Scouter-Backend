package com.yourssu.scouter.hrms.member.business

import com.yourssu.scouter.hrms.member.business.dto.MemberSyncResult

import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.auth.authentication.business.OAuth2Service
import com.yourssu.scouter.common.support.business.utils.SemesterConverter
import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.common.department.implement.Department
import com.yourssu.scouter.common.department.implement.DepartmentReader
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.common.support.implement.google.GoogleDriveFile
import com.yourssu.scouter.common.support.implement.google.GoogleDriveMimeType
import com.yourssu.scouter.common.support.implement.google.GoogleDriveQueryBuilder
import com.yourssu.scouter.common.support.implement.google.GoogleDriveReader
import com.yourssu.scouter.common.support.implement.google.GoogleFormsReader
import com.yourssu.scouter.hrms.support.business.utils.NicknameConverter
import com.yourssu.scouter.hrms.member.implement.Member
import com.yourssu.scouter.hrms.member.implement.MemberRole
import com.yourssu.scouter.hrms.member.implement.MemberState
import com.yourssu.scouter.hrms.member.implement.MemberSyncLog
import com.yourssu.scouter.hrms.member.implement.MemberSyncLogReader
import com.yourssu.scouter.hrms.member.implement.MemberSyncLogWriter
import java.time.LocalDate
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MemberSyncService(
    private val applicantReader: ApplicantReader,
    private val departmentReader: DepartmentReader,
    private val memberSyncLogReader: MemberSyncLogReader,
    private val memberSyncLogWriter: MemberSyncLogWriter,
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

        val (failureMessages, createdCount) =
            mergeToActiveMemberAndReturnFailMessages(acceptedApplicants, additionalInfos)

        return MemberSyncResult(
            failureMessages = failureMessages,
            createdCount = createdCount,
        )
    }

    private fun mergeToActiveMemberAndReturnFailMessages(
        acceptedApplicants: List<Applicant>,
        additionalInfos: List<AcceptedApplicantResponse>,
    ): Pair<List<String>, Int> {
        val departments: List<Department> = departmentReader.readAll()
        val failureMessages = mutableListOf<String>()
        var createdCount = 0
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
                stateUpdatedTime = Instant.now(),
            )

            val memberSyncLog = MemberSyncLog.create()

            memberSyncLogWriter.write(memberSyncLog)
            val created: Boolean = memberService.createMemberWithActiveStateIfNotExists(newMember)
            if (created) {
                createdCount += 1
            }
        }

        return failureMessages to createdCount
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

    fun readLastUpdatedTime(): Instant? {
        val lastLog: MemberSyncLog? = memberSyncLogReader.findLastLog()

        return lastLog?.syncTime
    }
}

data class AcceptedApplicantResponse(
    val studentId: String,
    val nickname: String,
    val yourssuEmail: String,
    val birthDate: LocalDate,
)
