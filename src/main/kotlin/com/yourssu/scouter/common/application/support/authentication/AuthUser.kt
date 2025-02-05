package com.yourssu.scouter.common.application.support.authentication

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthUser(
    val required: Boolean = true
)
