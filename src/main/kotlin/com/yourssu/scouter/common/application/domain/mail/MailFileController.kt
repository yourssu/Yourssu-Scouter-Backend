package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.common.business.domain.mail.MailFileService
import com.yourssu.scouter.common.implement.domain.mail.MailFilePresignCommand
import com.yourssu.scouter.common.implement.domain.mail.MailFileUsage
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFile
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "메일")
@RestController
@RequestMapping("/api/mails/files")
class MailFileController(
    private val mailFileService: MailFileService,
) {
    @Operation(summary = "메일 파일 업로드용 presigned URL 발급")
    @PostMapping("/presign")
    fun createPresignedUploadUrls(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestBody request: MailFilePresignRequest,
    ): ResponseEntity<MailFilePresignResponse> {
        val uploads =
            request.files.map {
                mailFileService.createPresignedPutUrl(
                    MailFilePresignCommand(
                        userId = authUserInfo.userId,
                        fileName = it.fileName,
                        contentType = it.contentType,
                        usage = it.usage,
                    ),
                )
            }

        return ResponseEntity.ok(
            MailFilePresignResponse(
                uploads =
                    uploads.map {
                        MailFilePresignResponse.PresignedUpload(
                            s3Key = it.s3Key,
                            putUrl = it.putUrl,
                            expiresAt = it.expiresAt,
                            contentType = it.contentType,
                        )
                    },
            ),
        )
    }

    @Operation(summary = "업로드 완료 파일 등록")
    @PostMapping("/confirm")
    fun confirmUploads(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestBody request: MailFileConfirmRequest,
    ): ResponseEntity<MailFileConfirmResponse> {
        val saved =
            mailFileService.confirmUploads(
                userId = authUserInfo.userId,
                files =
                    request.files.map {
                        MailUploadedFile(
                            userId = authUserInfo.userId,
                            usage = it.usage,
                            fileName = it.fileName,
                            contentType = it.contentType,
                            storageKey = it.s3Key,
                        )
                    },
            )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            MailFileConfirmResponse(
                files = saved.map { MailFileSummary.from(it) },
            ),
        )
    }

    @Operation(summary = "내 메일 파일 목록 조회")
    @GetMapping
    fun readFiles(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestParam(required = false) usage: MailFileUsage?,
    ): ResponseEntity<MailFileListResponse> {
        val files = mailFileService.readActiveFiles(authUserInfo.userId, usage)
        return ResponseEntity.ok(MailFileListResponse(files = files.map { MailFileSummary.from(it) }))
    }

    @Operation(summary = "내 메일 파일 삭제")
    @DeleteMapping("/{fileId}")
    fun deleteFile(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable fileId: Long,
    ): ResponseEntity<Unit> {
        mailFileService.deleteFile(authUserInfo.userId, fileId)
        return ResponseEntity.noContent().build()
    }
}

data class MailFilePresignRequest(
    @field:Schema(description = "업로드 대상 파일 목록")
    val files: List<FileSpec>,
) {
    data class FileSpec(
        @field:Schema(description = "파일명", example = "guide.pdf")
        val fileName: String,
        @field:Schema(description = "파일 MIME 타입", example = "application/pdf")
        val contentType: String,
        @field:Schema(description = "파일 용도", allowableValues = ["INLINE", "ATTACHMENT"])
        val usage: MailFileUsage,
    )
}

data class MailFilePresignResponse(
    val uploads: List<PresignedUpload>,
) {
    data class PresignedUpload(
        val s3Key: String,
        val putUrl: String,
        val expiresAt: java.time.Instant,
        val contentType: String,
    )
}

data class MailFileConfirmRequest(
    val files: List<File>,
) {
    data class File(
        @field:Schema(description = "S3 저장 키", example = "mail-files/inline/1/uuid-logo.png")
        val s3Key: String,
        @field:Schema(description = "파일명", example = "logo.png")
        val fileName: String,
        @field:Schema(description = "파일 MIME 타입", example = "image/png")
        val contentType: String,
        @field:Schema(description = "파일 용도", allowableValues = ["INLINE", "ATTACHMENT"])
        val usage: MailFileUsage,
    )
}

data class MailFileConfirmResponse(
    val files: List<MailFileSummary>,
)

data class MailFileListResponse(
    val files: List<MailFileSummary>,
)

data class MailFileSummary(
    val fileId: Long,
    val usage: MailFileUsage,
    val fileName: String,
    val contentType: String,
    val s3Key: String,
    val used: Boolean,
    val createdAt: java.time.Instant?,
) {
    companion object {
        fun from(file: MailUploadedFile): MailFileSummary {
            return MailFileSummary(
                fileId = file.id!!,
                usage = file.usage,
                fileName = file.fileName,
                contentType = file.contentType,
                s3Key = file.storageKey,
                used = file.used,
                createdAt = file.createdAt,
            )
        }
    }
}
