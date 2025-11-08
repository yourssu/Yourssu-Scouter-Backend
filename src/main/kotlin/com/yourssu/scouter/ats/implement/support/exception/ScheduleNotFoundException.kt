package com.yourssu.scouter.ats.implement.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class ScheduleNotFoundException(
    scheduleId: Long? = null,
) : CustomException(scheduleId?.let { "면접 일정을 찾을 수 없습니다: $it" } ?: "면접 일정을 찾을 수 없습니다",
    errorCode = "Schedule-003",
    status = HttpStatus.NOT_FOUND)