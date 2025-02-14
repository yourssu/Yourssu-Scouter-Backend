package com.yourssu.scouter.common.application.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import java.util.stream.Collectors
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CommonExceptionHandler {

    companion object {
        private const val METHOD_ARGUMENT_NOT_VALID_EXCEPTION_ERROR_CODE = "Request-Validation-Fail"
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
