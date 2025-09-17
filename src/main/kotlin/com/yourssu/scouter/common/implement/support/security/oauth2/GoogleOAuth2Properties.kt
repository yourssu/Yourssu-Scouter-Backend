package com.yourssu.scouter.common.implement.support.security.oauth2

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth2.google")
data class GoogleOAuth2Properties(
    val clientId: String,
    val clientSecret: String,
    // 기존 path는 널 허용 + 기본값 제공
    var redirectPath: String? = "/oauth2/callback/google",
    var allowedRedirectUris: List<String>? = null,
    val scope: List<String>
) {

    fun calculateRedirectUri(baseUrl: String? = null): String {
        // referer 등으로 들어온 baseUrl에서 오리진만 추출해 안전하게 붙임
        val origin = extractOrigin(baseUrl ?: "http://localhost:5173")
        val path = (redirectPath ?: "/oauth2/callback/google").let { p -> if (p.startsWith("/")) p else "/$p" }
        return origin.removeSuffix("/") + path
    }

    private fun extractOrigin(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val scheme = uri.scheme ?: "http"
            val host = uri.host ?: uri.authority ?: "localhost"
            val defaultPort = if (scheme.equals("https", ignoreCase = true)) 443 else 80
            val port = if (uri.port != -1 && uri.port != defaultPort) ":${uri.port}" else ""
            "$scheme://$host$port"
        } catch (_: Exception) {
            // fallback: 단순 문자열 처리
            val trimmed = url.trim()
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                trimmed.substringBefore("/", trimmed)
            } else {
                "http://" + trimmed.substringBefore("/", trimmed)
            }
        }
    }
}
