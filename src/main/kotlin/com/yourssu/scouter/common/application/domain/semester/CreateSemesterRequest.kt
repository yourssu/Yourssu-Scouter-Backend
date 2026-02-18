package com.yourssu.scouter.common.application.domain.semester

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class CreateSemesterRequest(

    @field:NotNull(message = "연도를 입력하지 않았습니다.")
    @field:Positive(message = "연도는 양수여야 합니다.")
    val year: Int,

    @field:NotNull(message = "학기를 입력하지 않았습니다.")
    @field:Positive(message = "학기는 양수여야 합니다.")
    val term: Int,
)
