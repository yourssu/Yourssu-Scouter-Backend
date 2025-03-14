package com.yourssu.scouter.common.implement.support.security.oauth2

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth2.google")
data class GoogleOAuth2Properties(
    val clientId: String,
    val clientSecret: String,
    val redirectPath: String,
    val scope: List<String>
) {

    fun calculateRedirectUri(referer: String): String {
        return URI(referer).resolve(redirectPath).toString()
    }
}
