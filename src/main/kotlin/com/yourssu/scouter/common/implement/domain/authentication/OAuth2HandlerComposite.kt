package com.yourssu.scouter.common.implement.domain.authentication

import com.yourssu.scouter.common.implement.support.exception.UnsupportedOAuth2LoginException
import org.springframework.stereotype.Component

@Component
class OAuth2HandlerComposite(
    handlers: Set<OAuth2Handler>
) {

    private val handlerMappings: Map<OAuth2Type, OAuth2Handler> = handlers.associateBy { it.getSupportingOAuth2Type() }

    fun findHandler(oauth2Type: OAuth2Type): OAuth2Handler {
        return handlerMappings[oauth2Type] ?: throw UnsupportedOAuth2LoginException("지원하는 OAuth2 타입이 아닙니다.")
    }
}
