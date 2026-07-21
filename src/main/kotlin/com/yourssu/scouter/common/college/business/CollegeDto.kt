package com.yourssu.scouter.common.college.business

import com.yourssu.scouter.common.college.implement.College

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
