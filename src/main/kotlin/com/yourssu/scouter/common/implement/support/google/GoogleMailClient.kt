package com.yourssu.scouter.common.implement.support.google

import com.yourssu.scouter.common.implement.support.exception.MailFailedException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class GoogleMailClient {

    fun sendEmail(encodedEmail: String, bearerAccessToken: String) {
        val requestBody = HttpRequest.BodyPublishers.ofString("""{"raw": "$encodedEmail"}""".trimIndent())
        val request = HttpRequest.newBuilder()
            .uri(java.net.URI.create("https://gmail.googleapis.com/gmail/v1/users/me/messages/send"))
            .header("Authorization", bearerAccessToken)
            .header("Content-Type", "application/json")
            .POST(requestBody).build()

        val client = HttpClient.newHttpClient()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() >= HttpStatus.BAD_REQUEST.value()) {
            throw MailFailedException("Failed to send email via Gmail API: ${response.statusCode()} ${response.body()}")
        }
    }
}
