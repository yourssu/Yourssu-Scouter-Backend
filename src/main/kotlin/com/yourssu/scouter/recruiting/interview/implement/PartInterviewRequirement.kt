package com.yourssu.scouter.recruiting.interview.implement

import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType

class PartInterviewRequirement(
    val id: Long? = null,
    val partId: Long,
    val semesterId: Long,
    val rubricType: RubricGroupType,
    val content: String,
)
