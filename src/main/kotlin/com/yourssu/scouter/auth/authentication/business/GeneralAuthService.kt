package com.yourssu.scouter.auth.authentication.business

import com.yourssu.scouter.auth.authentication.business.dto.LoginWithMemberResult
import com.yourssu.scouter.auth.authentication.business.dto.SignupResult
import com.yourssu.scouter.auth.authentication.implement.GeneralAuthValidator
import com.yourssu.scouter.auth.authentication.implement.PrivateClaims
import com.yourssu.scouter.auth.authentication.implement.Token
import com.yourssu.scouter.auth.authentication.implement.TokenProcessor
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserMemberLinker
import com.yourssu.scouter.auth.user.implement.UserWriter
import com.yourssu.scouter.member.core.business.dto.MemberDto
import com.yourssu.scouter.member.core.implement.Member
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class GeneralAuthService(
    private val userWriter: UserWriter,
    private val userMemberLinker: UserMemberLinker,
    private val generalAuthValidator: GeneralAuthValidator,
    private val tokenProcessor: TokenProcessor,
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

    fun login(email: String, password: String): LoginWithMemberResult {
        val user: User = generalAuthValidator.validateLoginCredentials(email, password)

        val tokenIssueTime = Instant.now()
        val privateClaims = PrivateClaims(user.id!!)
        val token: Token = tokenProcessor.generateToken(
            issueTime = tokenIssueTime,
            privateClaims = privateClaims.toMap(),
        )

        val member: Member = userMemberLinker.link(user.id, email)

        return LoginWithMemberResult(
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
            profileImageUrl = user.userInfo.profileImageUrl,
            member = MemberDto.from(member),
        )
    }
}
