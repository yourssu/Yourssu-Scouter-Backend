package com.yourssu.scouter.document.implement.domain.rubric

class DocumentSection(
    val id: Long? = null,
    val partId: Long,
    val question: String,
    val maxScore: Int,
    val criterionDetail: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentSection

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
