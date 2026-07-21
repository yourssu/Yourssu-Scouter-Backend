package com.yourssu.scouter.common.college.business

import com.yourssu.scouter.common.college.business.dto.ReadCollegesResult

import com.yourssu.scouter.common.college.implement.College
import com.yourssu.scouter.common.college.implement.CollegeReader
import org.springframework.stereotype.Service

@Service
class CollegeService(
    private val collegeReader: CollegeReader,
) {

    fun readAll(): ReadCollegesResult {
        val colleges: List<College> = collegeReader.readAll()

        return ReadCollegesResult.from(colleges)
    }
}
