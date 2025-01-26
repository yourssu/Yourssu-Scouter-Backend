package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState

/**
 * - “under-review”                 // 심사 진행 중
 * - “document-rejected”      // 서류 불합
 * - “interview-rejected”       // 면접 불합
 * - “incubating-rejected      // 인큐베이팅 불합
 * - “final-accepted”             // 최종 합격
 */
object ApplicantStateConverter {

    private val stateToString = mapOf(
        ApplicantState.UNDER_REVIEW to "심사 진행 중",
        ApplicantState.DOCUMENT_REJECTED to "서류 불합",
        ApplicantState.INTERVIEW_REJECTED to "면접 불합",
        ApplicantState.INCUBATING_REJECTED to "인큐베이팅 불합",
        ApplicantState.FINAL_ACCEPTED to "최종 합격",
    )

    private val stringToState = stateToString.entries.associate {
        it.value.replace(" ", "") to it.key
    }

    fun convertToString(state: ApplicantState): String = stateToString[state]
        ?: throw IllegalArgumentException("Unknown role: $state")

    fun convertToEnum(state: String): ApplicantState {
        val blankRemovedState = state.replace(" ", "")

        return stringToState[blankRemovedState]
            ?: throw IllegalArgumentException("Unknown Applicant State: $state")
    }
}
