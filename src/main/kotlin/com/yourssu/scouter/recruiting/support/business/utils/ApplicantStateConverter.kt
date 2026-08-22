package com.yourssu.scouter.recruiting.support.business.utils

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState

object ApplicantStateConverter {

    fun convertToEnum(state: String): ApplicantState =
        runCatching { ApplicantState.valueOf(state) }
            .getOrElse { throw IllegalArgumentException("허용되지 않는 state 값입니다: $state") }
}
