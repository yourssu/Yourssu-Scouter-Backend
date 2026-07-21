package com.yourssu.scouter.common.college.application

import com.yourssu.scouter.common.college.business.CollegeDto

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
