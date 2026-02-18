package com.yourssu.scouter.hrms.business.domain.authentication

import com.yourssu.scouter.common.business.domain.authentication.LoginResult
import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.hrms.business.domain.member.MemberDto
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.business.support.exception.MemberNotRegisteredException
import org.springframework.stereotype.Service

@Service
class LoginService(
    private val oauth2Service: OAuth2Service,
    private val memberReader: MemberReader,
) {

    fun login(
        oauth2Type: OAuth2Type,
        oauth2AuthorizationCode: String,
        referer: String,
        redirectUriOverride: String?,
    ): LoginWithMemberResult {
        val loginResult: LoginResult = oauth2Service.login(
            oauth2Type = oauth2Type,
            oauth2AuthorizationCode = oauth2AuthorizationCode,
            referer = referer,
            redirectUriOverride = redirectUriOverride,
        )

        val member: Member = memberReader.readByEmailOrNull(loginResult.email)
            ?: throw MemberNotRegisteredException("등록된 멤버가 아닙니다. 로그인할 수 없습니다.")

        return LoginWithMemberResult(
            accessToken = loginResult.accessToken,
            refreshToken = loginResult.refreshToken,
            profileImageUrl = loginResult.profileImageUrl,
            member = MemberDto.from(member),
        )
    }
}
