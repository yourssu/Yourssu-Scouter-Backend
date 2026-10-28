package com.yourssu.scouter.recruiting.rubric.implement

import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.recruiting.support.implement.exception.RubricLockedException
import org.springframework.stereotype.Component

/**
 * 파트+학기 면접 루브릭의 잠금 여부를 판정한다.
 *
 * 면접 평가가 한 번이라도 저장(임시저장 포함)되면 그 파트+학기 루브릭이 잠기고(InterviewEvaluationService.save),
 * 잠긴 뒤에는 평가가 근거한 루브릭/요구조건/질문 카탈로그를 바꿀 수 없다.
 * 판정 기준은 interview_rubric.is_locked 하나이며, 루브릭이 아직 없으면 잠기지 않은 것으로 본다.
 */
@Component
class InterviewRubricLockPolicy(
    private val interviewRubricReader: InterviewRubricReader,
    private val partReader: PartReader,
) {

    fun isLocked(partId: Long, semester: Semester): Boolean =
        interviewRubricReader.findByPartIdAndSemester(partId, semester)?.isLocked ?: false

    fun isAnyPartLocked(semester: Semester): Boolean =
        interviewRubricReader.findLockedPartIdsBySemester(semester).isNotEmpty()

    fun validateUnlocked(partId: Long, semester: Semester) {
        interviewRubricReader.findByPartIdAndSemester(partId, semester)?.validateEditable()
    }

    /** 학기 전체에 걸친 공유 데이터(전역 요구조건, INTRO/OUTRO/CULTURE 질문)는 어느 한 파트라도 잠기면 수정할 수 없다. */
    fun validateNoPartLocked(semester: Semester) {
        val lockedPartId = interviewRubricReader.findLockedPartIdsBySemester(semester).firstOrNull() ?: return
        val lockedPartName = partReader.readById(lockedPartId).name
        throw RubricLockedException("'$lockedPartName' 파트의 면접 루브릭이 잠겨 있어(면접 평가 존재) 수정할 수 없습니다.")
    }
}
