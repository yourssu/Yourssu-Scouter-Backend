package com.yourssu.scouter.auth.authentication.business

import com.yourssu.scouter.auth.authentication.business.dto.LoginWithMemberResult
import com.yourssu.scouter.auth.authentication.business.dto.SignupResult
import com.yourssu.scouter.auth.authentication.implement.GeneralAuthValidator
import com.yourssu.scouter.auth.authentication.implement.PrivateClaims
import com.yourssu.scouter.auth.authentication.implement.Token
import com.yourssu.scouter.auth.authentication.implement.TokenProcessor
import com.yourssu.scouter.auth.support.exception.DuplicateEmailException
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserMemberLinker
import com.yourssu.scouter.auth.user.implement.UserWriter
import com.yourssu.scouter.member.core.business.dto.MemberDto
import com.yourssu.scouter.member.core.implement.Member
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class GeneralAuthService(
    private val userWriter: UserWriter,
    private val userMemberLinker: UserMemberLinker,
    private val generalAuthValidator: GeneralAuthValidator,
    private val tokenProcessor: TokenProcessor,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun signup(email: String, password: String): SignupResult {
        val member: Member = generalAuthValidator.validateSignupEligible(email)

        val encodedPassword: String = passwordEncoder.encode(password)
        // 중복 확인과 저장 사이에는 락이 없어 동시 요청이 유니크 제약에 걸릴 수 있다.
        // 그대로 두면 400 Database-Constraint-Violation이 나가므로 409로 맞춰준다.
        val user: User = try {
            userWriter.writeGeneral(
                name = member.name,
                email = email,
                encodedPassword = encodedPassword,
            )
        } catch (e: DataIntegrityViolationException) {
            throw DuplicateEmailException("이미 가입된 이메일입니다.")
        }

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
