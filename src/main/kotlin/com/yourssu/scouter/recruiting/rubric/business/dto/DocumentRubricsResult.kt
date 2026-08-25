package com.yourssu.scouter.recruiting.rubric.business.dto

data class DocumentRubricsResult(
    val isLocked: Boolean,
    val sections: List<DocumentSectionDto>,
)
