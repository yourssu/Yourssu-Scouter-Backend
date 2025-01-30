package com.yourssu.scouter.common.implement.domain.semester

import java.time.LocalDate

enum class Term(val intValue: Int, val targetMonthRange: IntRange) {
    SPRING(1, 1..6),
    FALL(2, 7..12),
    ;

    companion object {
        fun of(date: LocalDate): Term {
            return entries.first {date.monthValue in it.targetMonthRange }
        }

        fun from(term: Int): Term {
            return entries.first { it.intValue == term }
        }
    }
}
