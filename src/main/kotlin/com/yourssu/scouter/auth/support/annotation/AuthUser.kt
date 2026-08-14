package com.yourssu.scouter.auth.support.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthUser(
    val required: Boolean = true
)
