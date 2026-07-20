package com.yourssu.scouter.document.implement.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class DocumentCommentSectionMismatchException(
    message: String,
) : CustomException(message, "DocumentComment-004", HttpStatus.BAD_REQUEST)
