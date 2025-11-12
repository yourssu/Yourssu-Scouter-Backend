package com.yourssu.scouter.hrms.implement.support

object AliasMappingUtils {

    fun normalizeKey(value: String): String {
        return value.lowercase().replace(" ", "").replace("-", "")
    }

    fun toCanonicalOrSelf(value: String, aliasMap: Map<String, String>): String {
        val normalizedMap = aliasMap.entries.associate { normalizeKey(it.key) to it.value }
        return normalizedMap[normalizeKey(value)] ?: value
    }
}
