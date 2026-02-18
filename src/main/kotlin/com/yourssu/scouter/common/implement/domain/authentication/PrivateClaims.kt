package com.yourssu.scouter.common.implement.domain.authentication

import io.jsonwebtoken.Claims

data class PrivateClaims(
    val userId: Long,
) {

    companion object {
        private const val USER_ID_KEY_NAME = "userId"

        fun from(claims: Claims): PrivateClaims {
            return PrivateClaims((claims[USER_ID_KEY_NAME] as Number).toLong())
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(USER_ID_KEY_NAME to userId)
    }
}
