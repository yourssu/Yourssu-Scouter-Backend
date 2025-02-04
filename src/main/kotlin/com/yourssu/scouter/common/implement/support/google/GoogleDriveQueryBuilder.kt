package com.yourssu.scouter.common.implement.support.google

class GoogleDriveQueryBuilder {
    private val conditions = mutableListOf<String>()

    fun nameContainsAll(vararg values: String): GoogleDriveQueryBuilder {
        values.forEach { value ->
            conditions.add("name contains '$value'")
        }
        return this
    }

    fun mimeType(vararg mimeTypes: GoogleDriveMimeType): GoogleDriveQueryBuilder {
        mimeTypes.forEach { mimeType ->
            conditions.add("mimeType = '${mimeType.value}'")
        }
        return this
    }

    fun createdTime(from: String, to: String): GoogleDriveQueryBuilder {
        conditions.add("createdTime >= '$from' and createdTime <= '$to'")

        return this
    }

    fun build(): String {
        return conditions.joinToString(" and ")
    }
}
