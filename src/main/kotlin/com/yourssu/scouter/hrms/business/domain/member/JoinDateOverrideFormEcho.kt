package com.yourssu.scouter.hrms.business.domain.member

/**
 * 가입일 매핑 재제출용 hidden 필드. 키는 `시트표시명|||가입일셀raw` 형태([MemberExcelImportOverrides.joinDateOverrides]).
 */
data class JoinDateOverrideFormEcho(
    val sheetLabel: String,
    val rawKey: String,
    val value: String,
) {
    companion object {
        fun fromOverrides(map: Map<String, String>): List<JoinDateOverrideFormEcho> =
            map.entries.mapNotNull { (k, v) ->
                val i = k.indexOf("|||")
                if (i < 0) null
                else JoinDateOverrideFormEcho(
                    sheetLabel = k.substring(0, i),
                    rawKey = k.substring(i + 3),
                    value = v,
                )
            }.sortedWith(compareBy({ it.sheetLabel }, { it.rawKey.isEmpty() }, { it.rawKey }))
    }
}
