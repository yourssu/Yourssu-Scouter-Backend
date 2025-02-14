package com.yourssu.scouter.common.application.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import java.util.stream.Collectors
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class CommonExceptionHandler: ResponseEntityExceptionHandler() {

    companion object {
        private const val METHOD_ARGUMENT_NOT_VALID_EXCEPTION_ERROR_CODE = "Request-Validation-Fail"
        private const val LOG_MESSAGE_FORMAT = "%s : %s"
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error(String.format(LOG_MESSAGE_FORMAT, ex.javaClass.simpleName, ex.message), ex)

        val response = ExceptionResponse(
            status = HttpStatus.valueOf(statusCode.value()),
            errorCode = "Internal-Server-Error",
            message = ex.message ?: "Internal server error"
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
    ): ResponseEntity<ExceptionResponse> {
        val message = e.fieldErrors
            .stream()
            .map { obj: FieldError -> obj.defaultMessage }
            .collect(Collectors.joining("\n"))

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse(HttpStatus.BAD_REQUEST, METHOD_ARGUMENT_NOT_VALID_EXCEPTION_ERROR_CODE, message))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ExceptionResponse> {
        val response = ExceptionResponse(
            status = HttpStatus.BAD_REQUEST,
            errorCode = "Request-Validation-Fail",
            message = e.message ?: "Request body is invalid"
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ExceptionResponse> {
        val response = ExceptionResponse(
            status = e.status,
            errorCode = e.errorCode,
            message = e.message
        )

        return ResponseEntity.status(e.status).body(response)
    }
}
