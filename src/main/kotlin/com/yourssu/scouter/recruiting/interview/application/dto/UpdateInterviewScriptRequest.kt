package com.yourssu.scouter.recruiting.interview.application.dto

import jakarta.validation.constraints.NotBlank

data class UpdateInterviewScriptRequest(

    @field:NotBlank
    val opening: String?,

    @field:NotBlank
    val closing: String?,
)
