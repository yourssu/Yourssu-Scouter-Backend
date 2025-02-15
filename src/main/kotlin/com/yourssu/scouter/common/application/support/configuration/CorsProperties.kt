package com.yourssu.scouter.common.application.support.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.cors")
data class CorsProperties(
    val allowedOrigins: Array<String>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CorsProperties

        return allowedOrigins.contentEquals(other.allowedOrigins)
    }

    override fun hashCode(): Int {
        return allowedOrigins.contentHashCode()
    }
}
