package com.yourssu.scouter.recruiting.question.implement

class FixedQuestion(
    val id: Long? = null,
    val category: FixedQuestionCategory,
    val content: String,
    val sortOrder: Int,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FixedQuestion

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
