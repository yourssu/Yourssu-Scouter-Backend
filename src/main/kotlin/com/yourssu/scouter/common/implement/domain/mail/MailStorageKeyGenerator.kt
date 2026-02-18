package com.yourssu.scouter.common.implement.domain.mail

import java.util.UUID

object MailStorageKeyGenerator {

    fun generate(usage: MailFileUsage, userId: Long, fileName: String): String {
        val category = when (usage) {
            MailFileUsage.INLINE -> "inline"
            MailFileUsage.ATTACHMENT -> "attachment"
        }
        return "$category/$userId/${UUID.randomUUID()}-${sanitize(fileName)}"
    }

    private fun sanitize(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
}
