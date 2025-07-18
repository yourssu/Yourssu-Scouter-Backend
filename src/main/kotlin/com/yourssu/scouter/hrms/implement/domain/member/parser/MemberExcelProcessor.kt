package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import org.apache.poi.ss.usermodel.Sheet

interface MemberExcelProcessor {

    fun supportingState(): MemberState

    fun parse(
        sheet: Sheet,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
    ): ErrorMessages
}
