package com.yourssu.scouter.common.implement.support.security.oauth2

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "googleOAuth2TokenClient", url = "https://oauth2.googleapis.com")
interface GoogleOAuth2TokenClient {

    @PostMapping(
        "/token",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun fetchToken(@RequestBody params: MultiValueMap<String, String>): GoogleTokenResponse
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleTokenResponse(
    val accessToken: String,
    val expiresIn: Long,
    val refreshToken: String? = null,
    val scope: String,
    val tokenType: String,
)
