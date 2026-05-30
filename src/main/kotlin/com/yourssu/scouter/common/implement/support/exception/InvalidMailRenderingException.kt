package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class InvalidMailRenderingException(
    message: String = "메일 렌더링 유효성 검사에 실패했습니다.",
) : CustomException(message, "Mail-Rendering-Validation-Fail", HttpStatus.BAD_REQUEST)
