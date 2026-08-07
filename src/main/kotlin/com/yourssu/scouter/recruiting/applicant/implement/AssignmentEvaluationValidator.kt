package com.yourssu.scouter.recruiting.applicant.implement

import com.yourssu.scouter.common.part.implement.Part
import com.yourssu.scouter.recruiting.support.implement.exception.AssignmentEvaluationNotAllowedException
import org.springframework.stereotype.Component

@Component
class AssignmentEvaluationValidator {

    fun validate(part: Part) {
        if (!part.hasAssignment) {
            throw AssignmentEvaluationNotAllowedException("해당 파트는 과제 평가를 사용하지 않습니다: ${part.name}")
        }
    }
}
