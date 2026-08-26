package com.yourssu.scouter.auth.authentication.implement

import com.yourssu.scouter.auth.support.exception.DuplicateEmailException
import com.yourssu.scouter.auth.support.exception.InvalidCredentialsException
import com.yourssu.scouter.auth.user.implement.AuthType
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.member.core.implement.Member
import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.member.support.exception.MemberNotRegisteredException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class GeneralAuthValidator(
    private val userReader: UserReader,
    private val memberReader: MemberReader,
    private val passwordEncoder: PasswordEncoder,
) {

    fun validateSignupEligible(email: String): Member {
        val member: Member = memberReader.readByEmailOrNull(email)
            ?: throw MemberNotRegisteredException("등록된 멤버가 아닙니다.")

        if (userReader.findByEmail(email) != null) {
            throw DuplicateEmailException("이미 가입된 이메일입니다.")
        }

        return member
    }

    fun validateLoginCredentials(email: String, password: String): User {
        val user: User = userReader.findByEmail(email)
            ?: throw InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.")

        // OAuth 유저는 password가 null이라 matches()가 false를 반환하긴 하지만,
        // 인코더 구현에 기대지 않고 인증 방식을 명시적으로 확인한다.
        if (user.userInfo.authType != AuthType.GENERAL) {
            throw InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.")
        }

        if (!passwordEncoder.matches(password, user.userInfo.password)) {
            throw InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.")
        }

        return user
    }
}
