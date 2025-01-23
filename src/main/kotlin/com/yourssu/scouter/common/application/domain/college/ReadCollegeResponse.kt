package com.yourssu.scouter.common.application.domain.college

import com.yourssu.scouter.common.business.domain.college.CollegeDto

data class ReadCollegeResponse(
    val id: Long,
    val name: String,
) {

    companion object {
        fun from(collegeDto: CollegeDto) = ReadCollegeResponse(
            id = collegeDto.id,
            name = collegeDto.name,
        )
    }
}
