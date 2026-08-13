package com.yourssu.scouter.hrms.support.business.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

/** API로 변경할 수 없는 멤버 필드를 본문에 포함한 경우 (Member-002와 구분). */
class MemberFieldNotEditableException(
    message: String,
) : CustomException(message, "Member-007", HttpStatus.BAD_REQUEST)
