package com.yourssu.scouter.common.application.domain.mail.template

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.common.business.domain.mail.template.MailTemplateService
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import org.springframework.http.ResponseEntity
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
}
