package com.yourssu.scouter.common.business.domain.college

import com.yourssu.scouter.common.implement.domain.college.College

data class ReadCollegesResult(
    val collegeDtos: List<CollegeDto>,
) {

    companion object {
        fun from(colleges: List<College>): ReadCollegesResult {
            return ReadCollegesResult(
                collegeDtos = colleges.map { CollegeDto.from(it) },
            )
        }
    }
}
