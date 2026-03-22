package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.hrms.implement.support.getFormattedStringSafe
import org.apache.poi.ss.usermodel.Row

/** 비액티브 시트 0행에서 사유·활동학기·예정복귀·문자 관련 열 인덱스를 찾는다. */
object InactiveSheetColumnResolver {

    private const val DEFAULT_COL_REASON = 10
    private const val DEFAULT_COL_ACTIVITY_SEMESTER = 11
    private const val DEFAULT_COL_EXPECTED_RETURN = 12
    private const val DEFAULT_COL_SMS_REPLIED = 13
    private const val DEFAULT_COL_SMS_REPLY_DESIRED_PERIOD = 14

    private const val MAX_HEADER_SCAN = 25

    fun resolveExtraColumns(headerRow: Row?): InactiveExtraColumnIndices {
        if (headerRow == null) {
            return InactiveExtraColumnIndices(
                reason = DEFAULT_COL_REASON,
                activitySemester = DEFAULT_COL_ACTIVITY_SEMESTER,
                expectedReturn = DEFAULT_COL_EXPECTED_RETURN,
                smsReplied = DEFAULT_COL_SMS_REPLIED,
                smsReplyDesiredPeriod = DEFAULT_COL_SMS_REPLY_DESIRED_PERIOD,
            )
        }
        return InactiveExtraColumnIndices(
            reason = findColumnIndex(headerRow, listOf("사유"), DEFAULT_COL_REASON),
            activitySemester = findColumnIndex(headerRow, listOf("활동학기", "활동 학기"), DEFAULT_COL_ACTIVITY_SEMESTER),
            expectedReturn = findColumnIndex(headerRow, listOf("예정복귀"), DEFAULT_COL_EXPECTED_RETURN),
            smsReplied = findColumnIndex(headerRow, listOf("문자회신여부", "문자회신 여부"), DEFAULT_COL_SMS_REPLIED),
            smsReplyDesiredPeriod = findColumnIndex(
                headerRow,
                listOf("문자회신희망시기", "문자회신 희망시기"),
                DEFAULT_COL_SMS_REPLY_DESIRED_PERIOD,
            ),
        )
    }

    private fun findColumnIndex(headerRow: Row, keywords: List<String>, defaultIndex: Int): Int {
        for (col in 0..MAX_HEADER_SCAN) {
            val cell = headerRow.getCell(col) ?: continue
            val text = cell.getFormattedStringSafe().trim().replace(" ", "")
            if (keywords.any { keyword -> text.contains(keyword.replace(" ", "")) }) {
                return col
            }
        }
        return defaultIndex
    }
}

data class InactiveExtraColumnIndices(
    val reason: Int,
    val activitySemester: Int,
    val expectedReturn: Int,
    val smsReplied: Int,
    val smsReplyDesiredPeriod: Int,
)
