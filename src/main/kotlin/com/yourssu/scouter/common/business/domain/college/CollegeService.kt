package com.yourssu.scouter.common.business.domain.college

import com.yourssu.scouter.common.implement.domain.college.College
import com.yourssu.scouter.common.implement.domain.college.CollegeReader
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
