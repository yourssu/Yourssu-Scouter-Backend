package com.yourssu.scouter.common.implement.domain.basetime

import java.time.LocalDateTime

open class BaseTime(
    createdTime: LocalDateTime? = null,
    val updatedTime: LocalDateTime? = null
) : BaseCreateTime(createdTime)
