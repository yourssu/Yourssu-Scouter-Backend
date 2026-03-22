package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.hrms.implement.domain.member.parser.ColumnNumberMapping
import com.yourssu.scouter.hrms.implement.support.getFormattedStringSafe
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import org.apache.poi.ss.usermodel.Sheet

/**
 * 수료 시트에서 학기로 해석되지 않는 11열 값을 distinct [rawKey]로 묶고,
 * 각 키에 해당하는 행의 이름·닉네임을 모은다 (엑셀에서 행을 찾기 위함).
 * 텍스트 셀은 [getFormattedStringSafe]가 그대로 반환한다.
 */
object CompletionSemesterMappingPreflight {

    fun collectHints(
        sheet: Sheet,
        resolveLabelToStoredSemester: (String) -> Semester?,
        columns: ColumnNumberMapping = ColumnNumberMapping.COMPLETED_MEMBER,
    ): List<CompletionSemesterMappingHint> {
        val byRaw = linkedMapOf<String, LinkedHashSet<Pair<String, String>>>()
        val rows = sheet.iterator().asSequence().drop(1)
        for (row in rows) {
            if (row.getCell(0).isNullOrBlank()) {
                break
            }
            val rawKey = row.getCell(11).getFormattedStringSafe().trim()
            if (resolveLabelToStoredSemester(rawKey) != null) {
                continue
            }
            val nameRaw = row.getCell(columns.name).getFormattedStringSafe().trim()
            val nickRaw = row.getCell(columns.nickname).getFormattedStringSafe().trim()
            val name = nameRaw.ifBlank { "(이름 없음)" }
            val nickname = nickRaw.ifBlank { "(닉 없음)" }
            val set = byRaw.getOrPut(rawKey) { linkedSetOf() }
            set.add(name to nickname)
        }
        return byRaw.entries
            .sortedWith(compareBy<Map.Entry<String, *>> { it.key.isEmpty() }.thenBy { it.key })
            .map { (rawKey, pairs) ->
                CompletionSemesterMappingHint(
                    rawKey = rawKey,
                    memberLabels = pairs.map { (n, nk) ->
                        CompletionSemesterMemberLabel(name = n, nickname = nk)
                    },
                )
            }
    }
}
