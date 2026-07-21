package com.yourssu.scouter.common.college.business.dto

import com.yourssu.scouter.common.college.implement.College

data class ReadCollegesResult(
    val collegeDtos: List<CollegeDto>,
) {

    companion object {
        fun from(colleges: List<College>): ReadCollegesResult = ReadCollegesResult(
            collegeDtos = colleges.map { CollegeDto.from(it) },
        )
    }
}
