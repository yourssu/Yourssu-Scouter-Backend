package com.yourssu.scouter.common.implement.support.google

import org.springframework.stereotype.Component

@Component
class GoogleDriveReader(
    private val googleDriveClient: GoogleDriveClient,
) {

    fun getFiles(authorizationHeader: String, query: String): List<GoogleDriveFile> {
        val response: GoogleDriveResponse = googleDriveClient.searchFiles(
            authorization = authorizationHeader,
            query = query,
        )

        return response.files
    }
}


