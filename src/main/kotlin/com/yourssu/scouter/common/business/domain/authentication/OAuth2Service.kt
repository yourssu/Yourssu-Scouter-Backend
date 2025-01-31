package com.yourssu.scouter.common.business.domain.authentication

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Handler
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2HandlerComposite
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import org.springframework.stereotype.Service

@Service
class OAuth2Service(
    private val oauth2HandlerComposite: OAuth2HandlerComposite,
) {

    fun getAuthCodeRequestUrl(oauth2Type: OAuth2Type): String {
        val oauth2Handler: OAuth2Handler = oauth2HandlerComposite.findHandler(oauth2Type)

        return oauth2Handler.provideAuthCodeRequestUrl()
    }
}
