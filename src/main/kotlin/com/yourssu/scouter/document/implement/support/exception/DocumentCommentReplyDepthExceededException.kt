package com.yourssu.scouter.document.implement.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class DocumentCommentReplyDepthExceededException(
    message: String,
) : CustomException(message, "DocumentComment-003", HttpStatus.BAD_REQUEST)
