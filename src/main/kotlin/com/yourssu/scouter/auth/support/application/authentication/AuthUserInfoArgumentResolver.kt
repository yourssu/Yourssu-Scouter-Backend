package com.yourssu.scouter.auth.support.application.authentication

import com.yourssu.scouter.common.support.application.exception.LoginRequiredException
import com.yourssu.scouter.auth.authentication.implement.PrivateClaims
import com.yourssu.scouter.auth.authentication.implement.TokenProcessor
import com.yourssu.scouter.auth.authentication.implement.TokenType
import com.yourssu.scouter.auth.support.implement.exception.InvalidTokenException
import com.yourssu.scouter.auth.support.implement.exception.InvalidTokenMessages
import io.jsonwebtoken.Claims
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.lang.NonNull
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthUserInfoArgumentResolver(
    private val tokenProcessor: TokenProcessor,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AuthUser::class.java) &&
                parameter.parameterType == AuthUserInfo::class.java
    }

    override fun resolveArgument(
        @NonNull parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val accessToken: String? = webRequest.getHeader(HttpHeaders.AUTHORIZATION)
        if (accessToken.isNullOrBlank()) {
            if (isRequired(parameter)) {
                throw LoginRequiredException("로그인이 필요한 기능입니다.")
            }

            return null
        }

        val claims: Claims = tokenProcessor.decode(TokenType.ACCESS, accessToken)
            ?: throw InvalidTokenException(InvalidTokenMessages.INVALID_TOKEN)
        val privateClaims = PrivateClaims.from(claims)

        return AuthUserInfo(privateClaims.userId)
    }

    private fun isRequired(parameter: MethodParameter): Boolean {
        return parameter.getParameterAnnotation(AuthUser::class.java)
            ?.required ?: true
    }
}
