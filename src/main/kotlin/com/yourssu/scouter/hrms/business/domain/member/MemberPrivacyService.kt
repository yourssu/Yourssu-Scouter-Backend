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
