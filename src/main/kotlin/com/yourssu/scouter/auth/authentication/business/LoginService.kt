package com.yourssu.scouter.auth.authentication.business

import com.yourssu.scouter.auth.authentication.business.dto.LoginWithMemberResult

import com.yourssu.scouter.auth.authentication.business.dto.LoginResult
import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.member.core.business.dto.MemberDto
import com.yourssu.scouter.member.core.implement.Member
import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.member.support.exception.MemberNotRegisteredException
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
