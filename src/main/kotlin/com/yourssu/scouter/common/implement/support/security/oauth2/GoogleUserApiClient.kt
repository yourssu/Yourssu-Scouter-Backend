package com.yourssu.scouter.common.implement.support.security.oauth2

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "googleUserApiClient", url = "https://www.googleapis.com")
interface GoogleUserApiClient {

    @GetMapping(
        "/oauth2/v2/userinfo",
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun fetchUserInfo(@RequestHeader("Authorization") accessToken: String): GoogleUserInfoResponse
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GoogleUserInfoResponse(
    val id: String,
    val name: String,
    val email: String,
    val picture: String,
    val familyName: String,
    val givenName: String,
    val verifiedEmail: Boolean,
)
