package com.yourssu.scouter.masterdata.college.business.dto

import com.yourssu.scouter.masterdata.college.implement.College

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
