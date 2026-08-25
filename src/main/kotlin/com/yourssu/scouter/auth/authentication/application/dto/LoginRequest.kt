package com.yourssu.scouter.auth.authentication.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(

    @field:NotBlank(message = "이메일이 입력되지 않았습니다.")
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호가 입력되지 않았습니다.")
    val password: String,
) {

    // data class의 기본 toString()은 비밀번호를 평문으로 노출하므로 마스킹한다.
    override fun toString(): String = "LoginRequest(email=$email, password=****)"
}
