package com.yourssu.scouter.auth.authentication.business

import com.yourssu.scouter.auth.authentication.business.dto.SignupResult
import com.yourssu.scouter.auth.authentication.implement.GeneralAuthValidator
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserWriter
import com.yourssu.scouter.member.core.implement.Member
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class GeneralAuthService(
    private val userWriter: UserWriter,
    private val generalAuthValidator: GeneralAuthValidator,
    private val passwordEncoder: PasswordEncoder,
) {

    fun signup(email: String, password: String): SignupResult {
        val member: Member = generalAuthValidator.validateSignupEligible(email)

        val encodedPassword: String = passwordEncoder.encode(password)
        val user: User = userWriter.writeGeneral(
            name = member.name,
            email = email,
            encodedPassword = encodedPassword,
        )

        return SignupResult(
            id = user.id!!,
            email = user.getEmailAddress(),
            name = member.name,
        )
    }
}
