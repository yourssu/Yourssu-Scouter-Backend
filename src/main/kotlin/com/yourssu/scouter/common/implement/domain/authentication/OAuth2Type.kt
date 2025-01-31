package com.yourssu.scouter.common.implement.domain.authentication

import com.yourssu.scouter.common.implement.support.exception.UnsupportedOAuth2LoginException

enum class OAuth2Type {

    GOOGLE,
    ;

    companion object {
        fun from(typeName: String): OAuth2Type {
            try {
                return valueOf(typeName.uppercase())
            } catch (e: IllegalArgumentException) {
                throw UnsupportedOAuth2LoginException("지원하는 OAuth2 타입이 아닙니다.")
            }
        }
    }
}
