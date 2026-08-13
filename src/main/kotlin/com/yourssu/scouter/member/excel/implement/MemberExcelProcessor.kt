package com.yourssu.scouter.member.excel.implement

import com.yourssu.scouter.masterdata.department.implement.Department
import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.member.excel.business.MemberExcelImportOverrides
import com.yourssu.scouter.member.core.implement.MemberState
import org.apache.poi.ss.usermodel.Sheet

interface MemberExcelProcessor {

    fun supportingState(): MemberState

    fun parse(
        sheet: Sheet,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        overrides: MemberExcelImportOverrides = MemberExcelImportOverrides.EMPTY,
    ): ErrorMessages
}
