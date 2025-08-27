package com.yourssu.scouter.common.implement.support.security.oauth2

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth2.google")
data class GoogleOAuth2Properties(
    val clientId: String,
    val clientSecret: String,
    // 기존 path는 널 허용 + 기본값 제공
    var redirectPath: String? = "/oauth/callback/google",

    // 새 절대 URL도 널 허용 (없으면 path로 계산)
    var redirectUri: String? = null,
    val scope: List<String>
) {

    fun calculateRedirectUri(baseUrl: String? = null): String {
        // redirectUri가 설정돼 있으면 무조건 그걸 사용
        redirectUri?.let { return it }

        // 하위호환: baseUrl + redirectPath 조합 (필요할 때만)
        val path = redirectPath ?: "/oauth/callback/google"
        val base = baseUrl ?: "http://localhost:3005"
        return "$base$path"
    }
}
