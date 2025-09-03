package com.yourssu.scouter.common.storage.domain.mail.template

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface JpaMailTemplateRepository : JpaRepository<MailTemplateEntity, Long> {
    fun findAllBy(pageable: Pageable): Page<MailTemplateEntity>
}
