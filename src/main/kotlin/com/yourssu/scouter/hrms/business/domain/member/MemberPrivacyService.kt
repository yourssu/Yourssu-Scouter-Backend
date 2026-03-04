package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import org.springframework.stereotype.Service

@Service
class MemberPrivacyService(
    private val userReader: UserReader,
    private val memberReader: MemberReader,
) {

    private val privilegedEmails: Set<String> = setOf(
        "umi.urssu@gmai.com",
        "feca.urssu@gmai.com",
        "nari.urssu@gmai.com",
        "emin.urssu@gmai.com",
        "piki.urssu@gmai.com",
    )

    fun isHrOrDev(userId: Long): Boolean {
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
