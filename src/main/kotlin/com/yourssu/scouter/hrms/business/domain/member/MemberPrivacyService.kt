package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.hrms.business.support.DevPrivilegeTestHolder
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MemberPrivacyService(
    private val userReader: UserReader,
    private val memberReader: MemberReader,
    @Autowired(required = false) private val devPrivilegeTestHolder: DevPrivilegeTestHolder? = null,
) {

    private val privilegedEmails: Set<String> = setOf(
        "umi.urssu@gmail.com",
        "feca.urssu@gmail.com",
        "nari.urssu@gmail.com",
        "emin.urssu@gmail.com",
        "piki.urssu@gmail.com",
        "logan.urssu@gmail.com",
    )

    /** 스카우터 팀원(privilegedEmails 목록) 여부. dev 어드민 API 호출 권한 판별용. */
    fun isScouterTeamMember(userId: Long): Boolean {
        val user = userReader.readById(userId)
        val email: String = user.getEmailAddress()
        return privilegedEmails.contains(email)
    }

    fun getMemberPartIds(userId: Long): Set<Long> {
        val user = userReader.readById(userId)
        val email: String = user.getEmailAddress()
        val member = memberReader.readByEmailOrNull(email) ?: return emptySet()
        return member.parts.mapNotNull { it.id }.toSet()
    }

    /**
     * 본인과 같은 파트(팀)에 속한 Active 멤버들의 이메일 목록을 반환한다.
     * - 사용자가 HRMS 멤버가 아니면(파트 정보를 알 수 없으면) 본인 이메일만 반환한다.
     */
    fun getActiveTeamMemberEmails(userId: Long): Set<String> {
        val user = userReader.readById(userId)
        val myEmail: String = user.getEmailAddress()
        val myPartIds: Set<Long> = getMemberPartIds(userId)
        if (myPartIds.isEmpty()) {
            return setOf(myEmail)
        }

        val activeMembers = memberReader.readAllActive()
        val teamEmails =
            activeMembers
                .asSequence()
                .map { active -> active.member }
                .filter { member -> member.parts.any { part -> part.id != null && myPartIds.contains(part.id) } }
                .map { member -> member.email }
                .toSet()

        return teamEmails + myEmail
    }

    fun isPrivilegedUser(userId: Long): Boolean {
        if (devPrivilegeTestHolder?.isMarkedAsNonPrivileged(userId) == true) {
            return false
        }
        val user = userReader.readById(userId)
        val email: String = user.getEmailAddress()

        if (privilegedEmails.contains(email)) {
            return true
        }

        val member = memberReader.readByEmailOrNull(email) ?: return false

        return member.parts.any { part ->
            part.name.equals("HR", ignoreCase = true)
        }
    }
}
