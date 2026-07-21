package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class DocumentCommentAccessDeniedException(
    message: String,
) : CustomException(message, "DocumentComment-002", HttpStatus.FORBIDDEN)
