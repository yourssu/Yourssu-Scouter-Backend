package com.yourssu.scouter.auth.authentication.implement

import com.yourssu.scouter.auth.support.exception.DuplicateEmailException
import com.yourssu.scouter.auth.user.implement.AuthType
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserReader
import org.springframework.stereotype.Component

/**
 * OAuth 로그인은 oauth_id로 기존 유저를 찾으므로, 같은 이메일이라도 oauth_id가 다르면
 * 새 유저를 만들려 한다. users.email에 유니크 제약이 생긴 뒤로는 그 INSERT가 DB 제약 위반으로
 * 실패해 400 Database-Constraint-Violation이 나가므로, 그 전에 도메인 예외로 걸러낸다.
 */
@Component
class OAuth2SignupValidator(
    private val userReader: UserReader,
) {

    fun validateEmailNotTaken(email: String) {
        val existingUser: User = userReader.findByEmail(email) ?: return

        if (existingUser.userInfo.authType == AuthType.GENERAL) {
            throw DuplicateEmailException("이미 이메일/비밀번호로 가입된 이메일입니다. 일반 로그인을 이용해 주세요.")
        }
        throw DuplicateEmailException("이미 다른 소셜 계정으로 가입된 이메일입니다.")
    }
}
