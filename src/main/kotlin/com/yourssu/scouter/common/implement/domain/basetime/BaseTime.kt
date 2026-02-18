package com.yourssu.scouter.common.implement.domain.basetime

import java.time.Instant

open class BaseTime(
    createdTime: Instant? = null,
    val updatedTime: Instant? = null
) : BaseCreateTime(createdTime)
