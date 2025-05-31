package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component

@Component
class WithdrawnMemberExcelProcessor : MemberExcelProcessor {

    override fun supportingState(): MemberState {
        return MemberState.WITHDRAWN
    }

    override fun parse(sheet: Sheet, departments: Map<String, Department>, parts: Map<String, Part>): ErrorMessages {
        return ErrorMessages(emptyList())
    }
}
