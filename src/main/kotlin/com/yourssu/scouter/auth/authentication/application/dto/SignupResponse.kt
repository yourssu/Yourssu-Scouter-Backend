package com.yourssu.scouter.auth.authentication.application.dto

import com.yourssu.scouter.auth.authentication.business.dto.SignupResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원가입 응답")
data class SignupResponse(
    @field:Schema(description = "유저 ID")
    val id: Long,
    @field:Schema(description = "이메일")
    val email: String,
    @field:Schema(description = "이름")
    val name: String,
) {
    companion object {
        fun from(result: SignupResult): SignupResponse = SignupResponse(
            id = result.id,
            email = result.email,
            name = result.name,
        )
    }
}
