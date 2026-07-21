package com.yourssu.scouter.common.basetime.implement

import java.time.Instant

open class BaseTime(
    createdTime: Instant? = null,
    val updatedTime: Instant? = null
) : BaseCreateTime(createdTime)
