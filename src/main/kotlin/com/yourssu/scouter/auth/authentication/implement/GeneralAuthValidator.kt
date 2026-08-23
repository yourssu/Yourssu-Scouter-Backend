package com.yourssu.scouter.auth.authentication.implement

import com.yourssu.scouter.auth.support.exception.DuplicateEmailException
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.member.core.implement.Member
import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.member.support.exception.MemberNotRegisteredException
import org.springframework.stereotype.Component

@Component
class GeneralAuthValidator(
    private val userReader: UserReader,
    private val memberReader: MemberReader,
) {

    fun validateSignupEligible(email: String): Member {
        val member: Member = memberReader.readByEmailOrNull(email)
            ?: throw MemberNotRegisteredException("등록된 멤버가 아닙니다.")

        if (userReader.findByEmail(email) != null) {
            throw DuplicateEmailException("이미 가입된 이메일입니다.")
        }

        return member
    }
}
