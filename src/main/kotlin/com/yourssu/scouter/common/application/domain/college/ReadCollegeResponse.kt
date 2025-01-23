package com.yourssu.scouter.common.application.domain.college

import com.yourssu.scouter.common.business.domain.college.CollegeDto

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
