package com.yourssu.scouter.common.business.domain.college

import com.yourssu.scouter.common.implement.domain.college.College

data class CollegeDto(
    val id: Long,
    val name: String,
) {

    companion object {
        fun from(college: College): CollegeDto = CollegeDto(
            id = college.id!!,
            name = college.name,
        )
    }
}
