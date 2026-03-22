package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.hrms.implement.support.getFormattedStringSafe
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import org.apache.poi.ss.usermodel.Row

/** 비액티브 시트에서 구간 제목 행·중복 헤더·데이터 끝을 구분한다. */
object InactiveSheetRowRules {

    private val cols: ColumnNumberMapping get() = ColumnNumberMapping.INACTIVE_MEMBER

    fun isEndOfTable(row: Row): Boolean =
        row.getCell(0).isNullOrBlank() && row.getCell(cols.name).isNullOrBlank()

    /** 이름이 비어 있으면 구간 제목(팀명만 채움) 등으로 보고 데이터 행이 아니다. */
    fun isNonDataRow(row: Row): Boolean = row.getCell(cols.name).isNullOrBlank()

    fun isDuplicateHeaderRow(row: Row): Boolean {
        fun token(col: Int): String = row.getCell(col).getFormattedStringSafe().trim().replace(" ", "")
        return token(cols.name) == "이름" && token(cols.studentId) == "학번"
    }
}
