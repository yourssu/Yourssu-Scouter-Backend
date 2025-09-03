package com.yourssu.scouter.common.application.domain.mail.template

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.common.business.domain.mail.template.MailTemplateService
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import org.springframework.http.ResponseEntity
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/mails/templates")
class MailTemplateController(
    private val mailTemplateService: MailTemplateService
) {

    @PostMapping
    fun create(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestBody request: CreateMailTemplateRequest,
    ): ResponseEntity<CreateMailTemplateResponse> {
        val domain: MailTemplate = request.toDomain(createdBy = authUserInfo.userId)
        val saved: MailTemplate = mailTemplateService.createTemplate(domain)
        return ResponseEntity.status(201).body(CreateMailTemplateResponse.from(saved))
    }

    @GetMapping
    fun readAll(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        @RequestParam(required = false, defaultValue = "updatedAt,desc") sort: String,
    ): ResponseEntity<PageResponse<ReadMailTemplateSummaryResponse>> {
        val sortParts = sort.split(",")
        val pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(
            if (sortParts.size >= 2 && sortParts[1].equals("desc", true)) org.springframework.data.domain.Sort.Order.desc(sortParts[0])
            else org.springframework.data.domain.Sort.Order.asc(sortParts[0])
        ))

        val pageResult = mailTemplateService.readTemplates(pageable)
        val response = PageResponse(
            content = pageResult.content.map { ReadMailTemplateSummaryResponse.from(it) },
            page = pageResult.number,
            size = pageResult.size,
            totalElements = pageResult.totalElements,
            totalPages = pageResult.totalPages,
        )
        return ResponseEntity.ok(response)
    }
}
