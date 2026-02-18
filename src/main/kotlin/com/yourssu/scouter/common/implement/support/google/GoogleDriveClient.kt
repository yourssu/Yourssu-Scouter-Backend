package com.yourssu.scouter.common.implement.support.google

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "googleDriveClient", url = "https://www.googleapis.com/drive/v3")
interface GoogleDriveClient {

    @GetMapping("/files")
    fun searchFiles(
        @RequestHeader("Authorization") authorization: String,
        @RequestParam("q") query: String,
        @RequestParam("fields") fields: String = "files(id, name, mimeType, webViewLink)"
    ): GoogleDriveResponse
}

data class GoogleDriveResponse(
    val files: List<GoogleDriveFile>
)

data class GoogleDriveFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val webViewLink: String
)
