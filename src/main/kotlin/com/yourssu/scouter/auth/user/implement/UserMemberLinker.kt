package com.yourssu.scouter.auth.user.implement

import com.yourssu.scouter.member.core.implement.Member
import com.yourssu.scouter.member.core.implement.MemberWriter
import com.yourssu.scouter.member.support.exception.MemberNotRegisteredException
import org.springframework.stereotype.Component

@Component
class UserMemberLinker(
    private val memberWriter: MemberWriter,
) {

    fun link(userId: Long, email: String): Member {
        return memberWriter.updateUserIdByEmail(userId, email)
            ?: throw MemberNotRegisteredException("등록된 멤버가 아닙니다.")
    }
}
