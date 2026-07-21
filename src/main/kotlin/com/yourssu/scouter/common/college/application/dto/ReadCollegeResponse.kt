package com.yourssu.scouter.common.college.application.dto

import com.yourssu.scouter.common.college.business.dto.CollegeDto

data class ReadCollegeResponse(
    val collegeId: Long,
    val collegeName: String,
) {

    companion object {
        fun from(collegeDto: CollegeDto) = ReadCollegeResponse(
            collegeId = collegeDto.id,
            collegeName = collegeDto.name,
        )
    }
}
