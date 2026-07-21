package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class DocumentCommentReplyDepthExceededException(
    message: String,
) : CustomException(message, "DocumentComment-003", HttpStatus.BAD_REQUEST)
